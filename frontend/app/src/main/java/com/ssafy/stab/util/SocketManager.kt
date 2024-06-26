package com.ssafy.stab.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.ssafy.stab.apis.space.folder.Folder
import com.ssafy.stab.apis.space.folder.Note
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.ssafy.stab.data.PreferencesUtil
import com.ssafy.stab.data.note.Action
import com.ssafy.stab.data.note.SocketPageInfo
import com.ssafy.stab.data.note.SocketPathInfo
import com.ssafy.stab.data.note.User
import com.ssafy.stab.screens.note.NoteViewModel
import com.ssafy.stab.screens.space.NoteListViewModel
import com.ssafy.stab.util.note.NoteControlViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URISyntaxException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
    on: 이벤트 받기, emit: 이벤트 보내기
    이벤트 보내기 : node.js와 데이터 호환성을 위해 json 형태로 데이터를 보냅니다.
    이벤트 받기 : json으로 보냈던 데이터를 받아 본인이 사용할 데이터 형태로 바꿉니다.
*/
class SocketManager private constructor() {
    private var socket: Socket? = null
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    var isConnected = false

    var isNoteJoined = false

    private var noteControlViewModel: NoteControlViewModel? = null

    private var noteViewModel: NoteViewModel? = null

    private val _userList = mutableStateListOf<User>()
    val userList: SnapshotStateList<User> = _userList


    private lateinit var viewModel: NoteListViewModel


    private val handler = Handler(Looper.getMainLooper())
    // 콜백 정의
    var onUserJoined: ((String) -> Unit)? = null
//    var onUserLeft: ((String) -> Unit)? = null // 사용자 떠날때

    fun setViewModel(viewModel: NoteListViewModel) {
        this.viewModel = viewModel
    }

    companion object {
        private var instance: SocketManager? = null

        fun getInstance(): SocketManager {
            if (instance == null) {
                instance = SocketManager()
            }
            return instance!!
        }
    }

    // NoteControlViewModel 설정 메서드
    fun setNoteControlViewModel(viewModel: NoteControlViewModel) {
        this.noteControlViewModel = viewModel
    }

    fun setNoteViewModel(viewModel: NoteViewModel) {
        this.noteViewModel = viewModel
    }

    // 소켓 연결
    fun connectToSocket(serverUrl: String) {
        if (isConnected) {
            Log.d("SocketManager", "Already connected")
            return
        }

        try {
            socket = IO.socket(serverUrl)
            Log.d("SocketManager", "Socket initialized with server URL: $serverUrl")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e("SocketManager", "URISyntaxException: ${e.message}")
            return
        }

        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("SocketConnection", "Connected")
            isConnected = true
        }
        socket?.on(Socket.EVENT_CONNECT_ERROR) { error ->
            Log.e("SocketConnection", "Connection error: ${error[0]}")
        }
        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketConnection", "Disconnected")
            isConnected = false
        }
        socket?.on("connectionSuccess") { data ->
            val socketId = data[0]
            Log.d("SocketConnection", "Connection successful with ID: $socketId")
        }

        registerEventHandlers()

        socket?.connect()
    }

    // 소켓 연결 종료
    fun disconnect() {
        socket?.disconnect()
        isConnected = false
    }

    // 스페이스 room 참여/떠나기
    fun joinSpace(spaceId: String, nickname: String) {
        if (isConnected) {
            socket?.emit("leaveSpace", spaceId)
            socket?.emit("joinSpace", spaceId, nickname)
        }
    }

    fun leaveSpace(spaceId: String) {
        socket?.emit("leaveSpace", spaceId)
    }

    // 스페이스 이벤트 업데이트 공유
    fun updateSpace(spaceId: String,  eventType: String, message: Any) {
        val jsonObject = JSONObject().apply {
            put("eventType", eventType)
            // 문자열이면 그대로 사용, 문자열이 아니면 직렬화
            if (message is String) {
                put("data", message)
            } else {
                put("data", JSONObject(gson.toJson(message)))
            }
        }
        val jsonData = jsonObject.toString()
        socket?.emit("updateSpace", spaceId, jsonData)
    }

    // 노트 room 참여/떠나기
    fun joinNote(noteId: String, nickName: String, profileImg: String) {
        isNoteJoined = true
        socket?.emit("joinNote", noteId, nickName, profileImg)
    }

    fun leaveNote(noteId: String) {
        isNoteJoined = false
        socket?.emit("leaveNote", noteId)
    }

    private fun updateUserList(data: Array<Any>) {
        userList.clear()
        val jsonString = gson.toJson(data[0])
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val nameValuePairs = jsonObject.getAsJsonObject("nameValuePairs")

        for ((_, value) in nameValuePairs.entrySet()) {
            if (value is JsonObject) {
                val nestedObject = value.asJsonObject
                val innerNameValuePairs = nestedObject.getAsJsonObject("nameValuePairs")
                val nickname = innerNameValuePairs.get("nickname").asString
                val profileImg = innerNameValuePairs.get("profileImg").asString
                val user = User(nickname, profileImg)
                userList.add(user)
            }
        }
    }

    // 노트 이벤트 업데이트 공유
    fun updateNoteData(noteId: String, message: Any) {
        val jsonData = gson.toJson(message)
        socket?.emit("updateDrawing", noteId, jsonData )
    }

    // 화면 따라가기 시작
    fun displayFollowing(socketId: String, nickname: String, color: String) {
        socket?.emit("displayFollowing", socketId, nickname, color)
    }

    // 화면 따라가기 종료
    fun stopFollowing(socketId: String) {
        socket?.emit("stopFollowing", socketId)
    }

    // 화면 이동
    fun positionMove(data: Any) {
        // 데이터를 JSON으로 변환해 전송
        val jsonData = gson.toJson(data)
        socket?.emit("positionMove", jsonData)
    }


    // 이벤트 수신 핸들러 - 서버로부터 받아오는 이벤트(데이터) 정리: 데이터는 배열의 형태로 들어옵니다 args[0]
    private fun registerEventHandlers() {
        // space 관련 서버 이벤트 수신
        socket?.on("spaceConnectUser") { data ->
            if (data.isNotEmpty()) {
                val jsonData = data[0].toString()

                // JSON 데이터를 SpaceRoom 객체로 변환하기 전에 데이터 구조 확인
                Log.d("SpaceConnection", "Received data: $jsonData")

                // JSON 데이터를 파싱하여 SpaceRoom 객체로 변환
                val spaceRoomType = object : TypeToken<Map<String, User>>() {}.type
                val users: Map<String, User> = gson.fromJson(jsonData, spaceRoomType)

                // spaceId를 별도로 관리하는 경우
                val currentSpaceId = PreferencesUtil.getShareSpaceState() // 클라이언트가 참여하고 있는 spaceId
                val spaceRoom = currentSpaceId?.let { SpaceRoom(spaceId = it, users = users) }

                // spaceRoom 및 users가 null이 아닌지 확인
                if (spaceRoom != null && spaceRoom.users.isNotEmpty()) {
                    // spaceId 출력
                    Log.d("SpaceConnection", "Connected SpaceRoom ID: ${spaceRoom.spaceId}")

                    // 각 사용자 정보 출력
                    spaceRoom.users.forEach { (socketId, user) ->
                        Log.d("SpaceConnection", "Socket ID: $socketId, User: $user")
                    }
                } else {
                    Log.e("SpaceConnection", "spaceRoom or users is null or empty")
                }
            } else {
                Log.e("SpaceConnection", "No data received from spaceConnectUser event")
            }
        }
            // space room에 입장한 사용자 닉네임 받기
            socket?.on("notifySpace") { data ->
                val remoteNickname = data[0]
                Log.d("SpaceConnection", "$remoteNickname just joined the Space Room")
            }
            // space 이벤트 받기
            socket?.on("receiveSpace") { message ->
                val data = message[0]
                Log.d("ReceiveSpaceData", "$data")
                handleSpaceMessage(data.toString())
            }

            // note 관련 서버 이벤트 수신
            socket?.on("noteConnectUser") { data ->
                updateUserList(data)
                Log.d("NoteConnection", userList.toList().toString())
            }

            // note room에 입장한 사용자 닉네임 받기
            socket?.on("notifyNote") { data ->
                val remoteNickname = data[0]
                Log.d("NoteConnection", "$remoteNickname just joined the Note Room")
                handler.post {
                    onUserJoined?.invoke(remoteNickname.toString())
                }

            }

            // note 이벤트 받기
            socket?.on("receiveDrawing") { message ->
                val data = message[0]
                val jsonData = data.toString()
                val jsonObject = JSONObject(jsonData)
                val type = jsonObject.getString("type")

                // 페이지 생성 액션
                if (type == "Create") {
                    val socketPageInfo = gson.fromJson(jsonData, SocketPageInfo::class.java)
                    noteViewModel?.socketAddPage(socketPageInfo)
                }
                // 경로 추가 액션
                else {
                    val socketPathInfo = gson.fromJson(jsonData, SocketPathInfo::class.java)
                    noteControlViewModel?.updatePathsFromSocket(socketPathInfo)
                }
                Log.d("ReceiveNoteData", type)
            }

            // followUser 이벤트 핸들러 - 보류
            socket?.on("followUser") { data ->
                Log.d("FollowUser", "$data")
            }

            // 화면 이동 위치 전송 받기 - 보류 (사유: data가 어떻게 오는지, 사용법을 잘 모르겠음)
            socket?.on("position") { data ->
                Log.d("Position", "$data")
            }

        }

    private fun handleSpaceMessage(message: String) {
        val jsonObject = JSONObject(message)
        val eventType = jsonObject.getString("eventType")
        val data = jsonObject.get("data") // data가 json 객체 또는 문자열

        when (eventType) {
            "NoteCreated" -> {
                val  note = gson.fromJson(data.toString(), Note::class.java)
                Log.d("ChekingInSocket", note.toString())
                viewModel.addNote(note)
            }
            "NoteUpdated" -> {
                val updateData = data as JSONObject
                val noteId = updateData.getString("noteId")
                val newTitle = updateData.getString("newTitle")
                Log.d("CheckingInSocket", "NoteUpdated: $noteId, newTitle: $newTitle")
                viewModel.renameNote(noteId, newTitle)
            }
            "NoteDeleted" -> {
                // 문자열 처리 조건 분기
                if (data is String) {
                    val noteId = data
                    Log.d("CheckingInSocket", "NoteDeleted: $noteId")
                    viewModel.deleteNote(noteId)
                } else {
                    Log.e("SocketManager", "Unexpected data type for NoteDeleted: $data")
                }
            }
            "FolderCreated" -> {
                val folder = gson.fromJson(data.toString(), Folder::class.java)
                Log.d("CheckingInSocket", folder.toString())
                viewModel.addFolder(folder)
            }
            "FolderUpdated" -> {
                val updateData = data as JSONObject
                val folderId = updateData.getString("folderId")
                val newTitle = updateData.getString("newTitle")
                Log.d("CheckingInSocket", "FolderUpdated: $folderId, newTitle: $newTitle")
                viewModel.renameNote(folderId, newTitle)
            }
            "FolderDeleted" -> {
                // 문자열 처리 조건 분기
                if (data is String) {
                    val folderId = data
                    Log.d("CheckingInSocket", "NoteDeleted: $folderId")
                    viewModel.deleteFolder(folderId)
                } else {
                    Log.e("SocketManager", "Unexpected data type for FolderDeleted: $data")
                }
            }
        }
    }


}

// gson localdatetime 처리를 위한 adapter
class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.format(formatter))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
        return LocalDateTime.parse(json.asString, formatter)
    }
}

data class User(
    val nickname: String,
    val profileImg: String,
    val color: String
)

data class SpaceRoom(
    val spaceId: String,
    val users: Map<String, User>
)