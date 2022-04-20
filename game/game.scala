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
  val authClientCredentials: RequestAuth = Spotify.Auth.clientCredentials()
  // Needs clinetId/secret and permissions scope, allows to control device
  var authCode: Option[RequestAuth] = None

  // Get playlists, select one
  val playlist = ???

  // Get songs from the playlist
  val playlistItems = Nil

  println("Starting web server on port 8080")
  @get("/")
  // Entry point - show link to login
  def entrypoint() = ???

  @get("/start")
  def start(code: String) = ???

  @get("/run")
  def run(): Response.Raw = {
    // Check if authenticated, if not show link to /login
    // else:
    // choose random song from playlists
    // Send request to Spotify to start playing the song on device

    // Schedule a request that would stop playing song after some delay
    // Show form where user can insert their guess, it should contain:
    // * some clue about the cong
    // * should send the reuslt to /submit using post and encoding type 'application/x-www-form-urlencoded'
    ???
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
    ???
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
  private final val ClientId = "03347033c0d845df8b8e8e1543e7a3e4"
  private final val ClientSecret = "6c3ae75b97e04cba8cd8f7c80a8371d0"
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
    // Build URL encoded list of parameters
    val params = ???
    s"$SpotifyAuthUrl/authorize?$params"

  object Auth {
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
  def listPlaylists(user: String): List[Playlist] = ???

  def listPlaylistItems(playlist: Playlist): List[PlaylistItem] = ???

  def play(playlist: Playlist, offset: Int) = ???

  def pause()(using auth: RequestAuth): Unit = ???

case class Playlist(uri: String, id: String, name: String)
case class PlaylistItem(track: Track)
case class Track(album: Album, name: String)
case class Album(name: String, artists: List[Artist])
case class Artist(name: String)

given Reader[Album] = macroR
given Reader[Artist] = macroR
given Reader[Playlist] = macroR
given Reader[PlaylistItem] = macroR
given Reader[Track] = macroR
