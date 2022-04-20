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
// Import read from package ujson using alias name 'parse'
import ujson.{read => parse}
import upickle.default.*
import com.github.vickumar1981.stringdistance.LevenshteinDistance

object CaskHttpServer extends cask.MainRoutes {
  val repo: Spotify.Repository = ???

  // Player to control device, needs auth-code
  var player: Option[Spotify.Player] = None

  @get("/")
  def entrypoint() =
    // Entry point - show link to login
    // Spotify should redirect to /start in the response
    // <div> click <a href=LoginUrl> here </a> to login </div>
    ???

  @get("/start")
  def start(code: String) =
    // Get the authorization code from the Spotify
    // Create an instance of Player
    // Redirect to /run
    ???

  @get("/run")
  def run(): Response.Raw = {
    // Check if authenticated, if not show link to /login
    // <div> Please try to <a href=loginUrl> login in </a> again to play
    // else:
    // choose random song from playlists
    // Send request to Spotify to start playing the song on device

    // Schedule a request that would stop playing song after some delay
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
    ???
  }

  @get("/login")
  def login() = {
    // Redirect to Spotify logining using OAuth
    Redirect(Spotify.Auth.AuthorizeUrl)
  }

  @cask.postForm("/submit")
  def submit(answer: String, guess: String) = {
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
    ???
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
    final val AuthorizeUrl: String =
      // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
      // https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
      import java.nio.charset.StandardCharsets
      import java.net.URLEncoder
      val scope = "user-read-playback-state user-modify-playback-state"
      val params = ???
      s"$SpotifyAuthUrl/authorize?$params"

    def clientCredentials(): RequestAuth =
      // Get access_token using client-credentials
      // https://developer.spotify.com/documentation/general/guides/authorization/client-credentials/
      // read access_token from response
      val accessToken = ???
      RequestAuth.Bearer(accessToken)

    def authorizationCode(code: String): RequestAuth =
      // Use authorization code to obtain auth code
      // https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
      val accessToken = ???
      RequestAuth.Bearer(accessToken)
  }

  // Repository of Spotify resources, needs only client-credentials auth
  class Repository()(using auth: RequestAuth) {
    // https://developer.spotify.com/console/get-playlists/
    // https://api.spotify.com/v1/users/{user_id}/playlists
    def listUserPlaylists(user: String): List[PlaylistRef] = ???

    // https://developer.spotify.com/console/get-playlist-tracks/
    // https://api.spotify.com/v1/playlists/{playlist_id}/tracks
    def listPlaylist(playlist: Playlist): List[PlaylistEntry] = ???
  }

  // Device controler, needs auth-code authorization
  class Player()(using auth: RequestAuth) {
    // https://developer.spotify.com/console/put-play/
    // https://api.spotify.com/v1/me/player/play
    def play(playlist: Playlist, trackIdx: Int) = ???

    // https://developer.spotify.com/console/put-pause/)
    // https://api.spotify.com/v1/me/player/pause
    def pause(): Unit = ???
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
