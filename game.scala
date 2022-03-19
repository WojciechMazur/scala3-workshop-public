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
import os.*
import upickle.default.*
import java.util.Base64
import java.nio.charset.StandardCharsets
import java.net.URLEncoder

import scala.concurrent.*
import scala.util.Random

object CaskHttpServer extends cask.MainRoutes {

  println("Logging client into Spotify")
  // Authorize user in the Spotify, get the RequestAuth
  // client credientails auth - allows to fetch playlists info
  val auth: RequestAuth = Spotify.clientCredentials()
  // Needs clinetId/secret and permissions scope, allows to control device
  var authCode: Option[RequestAuth] = None

  // Get playlists, select one
  val playlist = ???

  // Get songs from the playlist
  val playlistItems = ???

  println("Starting web server on port 8080")

  @get("/")
  // Entry point - show link to login
  def entrypoint() = ???

  @get("/start")
  def start(code: String) = {
    // Authorize in and get 'authCode', then and redirect to /run
    authCode = Some(Spotify.authorizationCode(code))
    ???
  }

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

    Response(
      Template(
        "Guess!",
        h1("Guess the song!"),
        div(s"Clue: ${???}"),
        form(
          action := "/submit",
          method := "post",
          enctype := UrlEncoded,
          autocomplete := false
        )(
          input(
            `type` := "hidden",
            name := "answer",
            // value := ???, // utf-64 encoded
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
    ???
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
  private final val ClientId = "18500f030b0f414890a38c16e7391116"
  private final val ClientSecret = "d015e1ed6d3c4a1082b9fd29b35688e5"
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
    // Url encoced paramaters string
    val params = ???
    s"$SpotifyAuthUrl/authorize?$params"

  def clientCredentials(): RequestAuth =
    // Get access_token using credentials
    // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
    // headers = Map("Content_Type" -> urlEncoded),
    // params = Map("grant_type" -> "client_credentials"),
    // read access_token from response
    ???

  def authorizationCode(code: String): RequestAuth =
    // Use authorization code to obtain access_token
    // https://developer.spotify.com/documentation/web-api/quick-start/ #Call the Spotify Accounts Service
    // s"$SpotifyAuthUrl/api/token"
    // headers = Map("Content_Type" -> UrlEncoded),
    // params = Map(
    //   "grant_type" -> "authorization_code",
    //   "code" -> code,
    //   "redirect_uri" -> RedirectUrl
    // )
    // read access_token from response

    ???

  def listPlaylists(user: String)(using auth: RequestAuth): List[Nothing] =
    // val playlists_url = s"$SpotifyApi/users/$user/playlists"
    ???

  def listPlaylistItems(playlist: Nothing): List[Nothing] =
    // s"$SpotifyApi/playlists/${playlist.id}/tracks"
    ???

  def play(playlist: Nothing, offset: Int)(using auth: RequestAuth) =
    s"$SpotifyApi/me/player/play"
    ???

  def pause()(using auth: RequestAuth): Unit =
    s"$SpotifyApi/me/player/pause"
    ???
