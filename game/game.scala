//> using scala "3.0.1"

//> using lib "com.lihaoyi::cask:0.8.0"
//> using lib "com.lihaoyi::requests:0.7.0"
//> using lib "com.lihaoyi::scalatags:0.11.1"
//> using lib "com.lihaoyi::os-lib:0.8.0"
//> using lib "com.lihaoyi::upickle:1.4.4"
//> using lib "com.github.vickumar1981:stringdistance_2.13:1.2.6"
//> using resourceDir "./styles/"

import cask.*
import requests.{Response => _, head => _, *}
import scalatags.Text.all.*
import upickle.default.*
import java.util.Base64
import java.nio.charset.StandardCharsets
import java.net.URLEncoder

import scala.concurrent.*
import scala.util.Random
import com.github.vickumar1981.stringdistance.LevenshteinDistance

object CaskHttpServer extends cask.MainRoutes {

  println("Logging client into Spotify")
  // Authorize user in the Spotify, get the RequestAuth
  // client credientails auth - allows to fetch playlists info
  val authClientCredentials: RequestAuth = Spotify.clientCredentials()
  // Needs clinetId/secret and permissions scope, allows to control device
  var authCode: Option[RequestAuth] = None

  // Get playlists, select one
  val playlist = Spotify
    .listPlaylists(user = "Spotify")(using authClientCredentials)
    .head

  // Get songs from the playlist
  val playlistItems = Spotify
    .listPlaylistItems(playlist)(using authClientCredentials)
    .tapEach(v => println(s"Got playlist item ${v}"))
    .toList

  println("Starting web server on port 8080")

  @get("/")
  // Entry point - show link to login
  def entrypoint() = Template(
    title = "Hello",
    h1("Guess the song game!"),
    div(
      "Please click here to login in Spotify",
      a(href := Spotify.LoginUrl)("HERE")
    )
  )

  @get("/start")
  def start(code: String) = {
    // Authorize in and get 'authCode', then and redirect to /run
    authCode = Some(Spotify.authorizationCode(code))
    Redirect("/run")
  }

  @get("/run")
  def run(): Response.Raw = {
    if authCode.isEmpty then
      return Template(
        "Authorization failed",
        h1("Authorization failed"),
        div(
          "Please try ",
          a(href := Spotify.LoginUrl)("log in"),
          " again to play."
        )
      )

    given RequestAuth = authCode.get

    // Check if authenticated, if not show link to /login
    // else:
    // choose random song from playlists
    val songIndex = scala.util.Random.nextInt(playlistItems.size)
    val song = playlistItems(songIndex)
    // Send request to Spotify to start playing the song on device

    Spotify.play(playlist, songIndex)

    Future {
      Thread.sleep(8 * 1000)
      Spotify.pause()
    }

    // Schedule a request that would stop playing song after some delay
    // Show form where user can insert their guess, it should contain:
    // * some clue about the cong
    // * should send the reuslt to /submit using post and encoding type 'application/x-www-form-urlencoded'
    Response(
      Template(
        "Guess!",
        h1("Guess the song!"),
        div(s"Clue: ${playlistItems(songIndex).track}"),
        form(
          action := "/submit",
          method := "post",
          enctype := UrlEncoded,
          autocomplete := false
        )(
          input(
            `type` := "hidden",
            name := "answer",
            value := s"${Base64.getEncoder.encode(song.track.name.getBytes)}", // base-64 encoded
            width := "0%"
          ),
          input(`type` := "text", name := "guess", width := "80%"),
          input(`type` := "submit", value := "Guess", width := "20%")
        )
      )
    )
  }

  @get("/login")
  def login() = {
    // Redirect to Spotify logining using OAuth
    Redirect(Spotify.LoginUrl)
  }

  @cask.postForm("/submit")
  def submit(answer: String, guess: String) = {
    // Check the guess and correct answer
    // Show message wheter message is correct or not
    // Show a link to alowing to retry with different song
    val name = new String(
      Base64.getDecoder.decode(answer),
      StandardCharsets.UTF_8
    )
    val answerMatching = LevenshteinDistance.distance(
      name.toLowerCase(),
      guess.toLowerCase()
    )
    Template(
      "Your results...",
      h1(
        if answerMatching < 4
        then "Well done!"
        else "Whoops..."
      ),
      div(
        s"The correct answer was ${name}. ",
        a(href := "/run")("Another go?")
      )
    )
  }

  initialize()
}

object Template:
  private lazy val styles =
    tag("style")(os.read(os.resource(getClass.getClassLoader) / "styles.css"))
  def apply(
      title: String,
      content: scalatags.generic.Modifier[scalatags.text.Builder]*
  ): scalatags.Text.all.doctype =
    doctype("html")(html(styles, head(tag("title")(title)), body(content: _*)))

private final val UrlEncoded = "application/x-www-form-urlencoded"
object Spotify:
  private final val ClientId = "1588c59aabee43ca9f4d30d5695a4a0c"
  private final val ClientSecret = "d6dc6fb3f2e54982ab4af782ecb75a0e"
  private final val ClientAuth = RequestAuth.Basic(ClientId, ClientSecret)
  private final val RedirectUrl = "http://localhost:8080/start"
  private final val SpotifyApi = "https://api.spotify.com/v1"
  private final val SpotifyAuthUrl = "https://accounts.spotify.com"

  final val LoginUrl: String =
    // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
    val parameters = Map(
      "client_id" -> ClientId,
      "response_type" -> "code",
      "redirect_uri" -> RedirectUrl,
      "scope" -> "user-read-playback-state user-modify-playback-state"
    )
    val params = parameters
      .map { (key, value) =>
        s"$key=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
      }
      .mkString("&")
    s"$SpotifyAuthUrl/authorize?$params"

  def clientCredentials(): RequestAuth =
    // Get access_token using credentials
    // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
    // read access_token from response
    val result = requests.post(
      s"${SpotifyAuthUrl}/api/token",
      headers = Map("Content_Type" -> UrlEncoded),
      params = Map("grant_type" -> "client_credentials"),
      auth = ClientAuth
    )
    val responseJson = ujson.read(result.text())
    val accessToken = read[String](responseJson("access_token"))
    RequestAuth.Bearer(accessToken)

  def authorizationCode(code: String): RequestAuth =
    // Use authorization code to obtain access_token
    // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
    val result = requests.post(
      s"$SpotifyAuthUrl/api/token",
      headers = Map("Content_Type" -> UrlEncoded),
      params = Map(
        "grant_type" -> "authorization_code",
        "code" -> code,
        "redirect_uri" -> RedirectUrl
      ),
      auth = ClientAuth
    )
    val responseJson = ujson.read(result.text())
    val accessToken = read[String](responseJson("access_token"))
    RequestAuth.Bearer(accessToken)

  def listPlaylists(user: String)(using auth: RequestAuth): List[Playlist] =
    val playlists_url = s"$SpotifyApi/users/$user/playlists"
    val result = requests.get(playlists_url, auth = auth)
    import ujson.{read => parse}
    val resultJson = parse(result.text())
    read[List[Playlist]](resultJson("items"))

  def listPlaylistItems(playlist: Playlist)(using
      auth: RequestAuth
  ): List[PlaylistItem] =
    val result =
      requests.get(s"$SpotifyApi/playlists/${playlist.id}/tracks", auth = auth)
    val resultJson = ujson.read(result.text())
    read[List[PlaylistItem]](resultJson("items"))

  def play(playlist: Playlist, offset: Int)(using auth: RequestAuth) =
    case class Body(context_uri: String, offset: Offset, position_ms: Int)
    case class Offset(position: Int)
    given Writer[Body] = macroW
    given Writer[Offset] = macroW

    println(write(Body(playlist.uri, Offset(offset), 15)))
    val result = requests.put(
      s"$SpotifyApi/me/player/play",
      auth = auth,
      data = write(Body(playlist.uri, Offset(offset), 15))
    )

  def pause()(using auth: RequestAuth): Unit =
    requests.put(s"$SpotifyApi/me/player/pause", auth = auth)

case class Playlist(uri: String, id: String, name: String)
object Playlist {
  given Reader[Playlist] = macroR
}

case class PlaylistItem(track: Track)
object PlaylistItem {
  given Reader[PlaylistItem] = macroR
}
case class Track(album: Album, name: String)
object Track {
  given Reader[Track] = macroR
}
case class Album(name: String, artists: List[Artist])
object Album {
  given Reader[Album] = macroR
}
case class Artist(name: String)
object Artist {
  given Reader[Artist] = macroR
}
