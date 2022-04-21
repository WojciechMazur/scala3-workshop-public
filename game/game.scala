//> using lib "com.lihaoyi::cask:0.8.0"       // https://com-lihaoyi.github.io/cask/
//> using lib "com.lihaoyi::upickle:1.4.4"    // https://com-lihaoyi.github.io/upickle/
//> using lib "com.lihaoyi::scalatags:0.11.1" // https://com-lihaoyi.github.io/scalatags/
//> using lib "com.lihaoyi::requests:0.7.0"   // https://github.com/com-lihaoyi/requests-scala
//> using lib "com.lihaoyi::os-lib:0.8.0"     // https://github.com/com-lihaoyi/os-lib
//> using lib "com.github.vickumar1981:stringdistance_2.13:1.2.6" // https://github.com/vickumar1981/stringdistance
//> using resourceDir "./styles/"

import cask.*
// Import everything from requests except Response and head
import requests.{Response => _, head => _, *}
import scalatags.Text.all.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
// Import read from package ujson using alias name 'parse'
import ujson.{read => parse}
import upickle.default.*
import com.github.vickumar1981.stringdistance.LevenshteinDistance
import java.util.Base64

object CaskHttpServer extends cask.MainRoutes {
  import Spotify._
  val repo = Auth
    .clientCredentials()
    .map(Repository()(using _))
    .getOrElse(sys.error("Incorrect client credentials"))
  val playlist = repo.listUserPlaylists("Spotify").head
  val tracks = repo
    .listPlaylist(playlist)
    .map(_.track)
    .tapEach(println)

  // Player to control device, needs auth-code received after login
  private var player: Option[Spotify.Player] = None

  @get("/")
  def entrypoint() =
    // Entry point - show link to login
    // Spotify should redirect to /start in the response
    // <div> click <a href=LoginUrl> here </a> to login </div>
    Template("Guess the song!") {
      div(
        "Please click ",
        a(href := Spotify.Auth.AuthorizeUrl)("here"),
        " to login."
      )
    }

  @get("/start")
  def start(code: String) =
    // Get the authorization code from the Spotify
    // Create an instance of Player and redirect to run
    // or show information about failure and try to login again
    Spotify.Auth
      .authorizationCode(code)
      .map { implicit auth =>
        player = Some(Player())
        Redirect("/run")
      }
      .getOrElse {
        Response(
          Template("Failed to login")(
            div(
              "Failed to login to Spotify using, please try again ",
              a(href := Spotify.Auth.AuthorizeUrl)("here"),
              " to login."
            )
          ).render
        )
      }

  @get("/run")
  def run() = {
    // choose random song from playlists
    val songIndex = scala.util.Random.nextInt(tracks.size)
    val song = tracks(songIndex)
    val player = this.player.get
    lazy val clue = {
      val splitWords: String => List[String] =
        _.split(' ').filterNot(_.isBlank).toList
      def firstAndLastLetters(sentence: String) =
        splitWords(sentence)
          .map(word =>
            if word.size == 2 then word
            else if word.size > 2 then s"${word.head}...${word.last}"
            else word.head
          )
      def initialLetters(sentence: String) =
        splitWords(sentence).flatMap(_.headOption)
      val songName = firstAndLastLetters(song.name).mkString(" ")
      val authors = song.album.artists
        .map(_.name)
        .map(initialLetters(_).mkString(""))
        .mkString(" and ")
      s"$songName by ${authors}"
    }

    // Send request to Spotify to start playing the song on device
    player.play(playlist, songIndex) match {
      case Player.State.NoActiveDevice =>
        Template("No active device")(
          div(
            "It looks like none of your Spotify clients is currently active.",
            "Try to use your Spotify client and ",
            b(a(href := "/run", "try again"))
          )
        )

      case Player.State.Ok =>
        // Schedule a request that would stop playing song after some delay
        Future {
          Thread.sleep(8 * 1000)
          player.pause()
        }

        // Show form where user can insert their guess, it should contain:
        // * some clue about the cong
        // * should send the reuslt to /submit using post and encoding type 'application/x-www-form-urlencoded'
        /* <div> Clue ... </div>
         * <form action=/submit method=post, enctype=UrlEncoded, autocomplete=fales>
         *  <input type=hidden name=answer value=CorrectAnswers, widhth=0%>
         *  <input type=text, name=guess/>
         *  <input type=submit value=Guess/>
         * </form>
         */

        Template("Guess!")(
          h1("Your turn, guess the song!"),
          div(s"Clue: $clue"),
          form(
            action := "/submit",
            method := "post",
            enctype := UrlEncoded,
            autocomplete := false
          )(
            input(
              `type` := "hidden",
              name := "encodedAnswer",
              value := new String(
                Base64.getEncoder.encode(song.name.getBytes)
              ),
              width := "0%"
            ),
            input(`type` := "text", name := "guess", width := "80%"),
            input(`type` := "submit", value := "Guess", width := "20%")
          )
        )
    }
  }

  @get("/login")
  def login() = {
    // Redirect to Spotify logining using OAuth
    Redirect(Spotify.Auth.AuthorizeUrl)
  }

  @cask.postForm("/submit")
  def submit(encodedAnswer: String, guess: String) = {
    val answer = String(Base64.getDecoder.decode(encodedAnswer))
    // Check the guess and correct answer
    // Show message wheter message is correct or not
    // Show a link to alowing to retry with different song
    /*
     * <h1>Result</h1>
     * <div>
     *   Correct answer was ...
     *   <a href=/run> Click to try again </a>
     * </div>
     */
    val matchIndex = LevenshteinDistance.distance(answer, guess)
    val isCorrect = matchIndex <= 6
    Template("Result") {
      div(
        h1(if isCorrect then "Correct!" else "Sorry, that's something else!"),
        div(
          p(s"Correct answer ${answer}"),
          p(s"Your asnwer ${guess}")
        ),
        div(a(href := "/run", "Click here to try again"))
      )
    }
  }

  println("Starting web server on port 8080...")
  initialize()
}

object Template:
  // A common template for all the responses
  def apply(title: String)(
      content: scalatags.generic.Modifier[scalatags.text.Builder]*
  ): scalatags.Text.all.doctype =
    doctype("html")(
      html(styles, head(tag("title")(title)), body(content: _*))
    )

  private lazy val styles =
    tag("style")(
      os.read(os.resource(getClass.getClassLoader) / "styles.css")
    )
end Template

private final val UrlEncoded = "application/x-www-form-urlencoded"
object Spotify:
  private final val ClientId = "03347033c0d845df8b8e8e1543e7a3e4"
  private final val ClientSecret = "6c3ae75b97e04cba8cd8f7c80a8371d0"
  private final val ClientAuth = RequestAuth.Basic(ClientId, ClientSecret)
  private final val RedirectUrl = "http://localhost:8080/start"
  private final val SpotifyApi = "https://api.spotify.com/v1"
  private final val SpotifyAuthUrl = "https://accounts.spotify.com"

  object Auth {
    final lazy val AuthorizeUrl: String =
      // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
      // https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
      import java.nio.charset.StandardCharsets.UTF_8
      import java.net.URLEncoder.encode
      val scope = "user-read-playback-state user-modify-playback-state"
      val params = Map(
        "client_id" -> ClientId,
        "response_type" -> "code",
        "redirect_uri" -> RedirectUrl,
        "scope" -> scope
      ).map((key, value) => s"$key=${encode(value, UTF_8)}")
        .mkString("&")
      s"$SpotifyAuthUrl/authorize?$params"

    def clientCredentials(): Option[RequestAuth] =
      // Get access_token using client-credentials
      // https://developer.spotify.com/documentation/general/guides/authorization/client-credentials/
      // read access_token from response
      val response = requests.post(
        url = s"$SpotifyAuthUrl/api/token",
        params = Map("grant_type" -> "client_credentials"),
        auth = ClientAuth
      )
      if response.is2xx then
        val accessToken = parse(response.text())("access_token").str
        Some(RequestAuth.Bearer(accessToken))
      else None

    def authorizationCode(code: String): Option[RequestAuth] =
      // Use authorization code to obtain auth code
      // https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
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
      if result.is2xx then
        val responseJson = ujson.read(result.text())
        val accessToken = read[String](responseJson("access_token"))
        Some(RequestAuth.Bearer(accessToken))
      else None
  }

  // Repository of Spotify resources, needs only client-credentials auth
  class Repository()(using auth: RequestAuth) {
    // https://developer.spotify.com/console/get-playlists/
    // https://api.spotify.com/v1/users/{user_id}/playlists
    def listUserPlaylists(user: String): List[PlaylistRef] = {
      val respone = requests.get(
        url = s"${SpotifyApi}/users/${user}/playlists",
        auth = auth
      )
      val json = parse(respone.text())
      read[List[PlaylistRef]](json("items"))
    }

    // https://developer.spotify.com/console/get-playlist-tracks/
    // https://api.spotify.com/v1/playlists/{playlist_id}/tracks
    def listPlaylist(playlist: PlaylistRef): List[PlaylistEntry] =
      val response = requests.get(
        url = s"${SpotifyApi}/playlists/${playlist.id}/tracks",
        auth = auth
      )
      val json = parse(response.text())
      read[List[PlaylistEntry]](json("items"))
  }

  // Device controler, needs auth-code authorization
  object Player {
    enum State:
      case Ok, NoActiveDevice
  }
  class Player()(using auth: RequestAuth) {
    import Player.*

    // https://developer.spotify.com/console/put-play/
    // https://api.spotify.com/v1/me/player/play
    def play(playlist: PlaylistRef, trackIdx: Int) = {
      val response = requests.put(
        url = s"$SpotifyApi/me/player/play",
        auth = auth,
        check = false,
        data = ujson.Obj(
          "context_uri" -> playlist.uri,
          "offset" -> ujson.Obj("position" -> trackIdx),
          "position_ms" -> 15
        )
      )
      lazy val data = parse(response.text())
      if response.is4xx &&
        data("error")("reason").strOpt.contains("NO_ACTIVE_DEVICE")
      then State.NoActiveDevice
      else State.Ok

    }
    // https://developer.spotify.com/console/put-pause/)
    // https://api.spotify.com/v1/me/player/pause
    def pause(): Unit =
      requests.put(s"$SpotifyApi/me/player/pause", auth = auth)

  }

case class Playlist(items: List[PlaylistEntry])
case class PlaylistRef(uri: String, name: String, id: String)
case class PlaylistEntry(track: Track)
case class Track(album: Album, name: String)
case class Album(name: String, artists: List[Artist])
case class Artist(name: String)

// Macro generated json readers
given Reader[Playlist] = macroR
given Reader[PlaylistRef] = macroR
given Reader[PlaylistEntry] = macroR
given Reader[Track] = macroR
given Reader[Album] = macroR
given Reader[Artist] = macroR
