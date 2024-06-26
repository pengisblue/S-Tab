package com.ssafy.stab.apis.space.share

import android.util.Log
import com.ssafy.stab.apis.RetrofitClient
import com.ssafy.stab.apis.space.folder.FileListResponse
import com.ssafy.stab.apis.space.folder.Folder
import com.ssafy.stab.apis.space.folder.Note
import com.ssafy.stab.data.PreferencesUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val apiService: ApiService = RetrofitClient.instance.create(ApiService::class.java)
private val accessToken = PreferencesUtil.getLoginDetails().accessToken
private val authorizationHeader = "Bearer $accessToken"


fun getShareSpace(spaceId: String, onResult: (ShareSpace) -> Unit) {
    val call = apiService.getShareSpace(authorizationHeader, spaceId)

    call.enqueue(object : Callback<ShareSpace> {
        override fun onResponse(call: Call<ShareSpace>, response: Response<ShareSpace>) {
            if (response.isSuccessful) {
                Log.d("APIResponse", response.body().toString())
                response.body()?.let { onResult(it) }
            } else {
                println("Response not successful: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(p0: Call<ShareSpace>, p1: Throwable) {
            Log.d("APIResponse", "요청 실패")
        }
    })
}

fun getShareSpaceList(onResult: (List<ShareSpaceList>) -> Unit) {
    val call = apiService.getShareSpaceList(authorizationHeader)

    call.enqueue(object: Callback<List<ShareSpaceList>> {
        override fun onResponse(
            call: Call<List<ShareSpaceList>>,
            response: Response<List<ShareSpaceList>>
        ) {
            if (response.isSuccessful) {
                Log.d("APIResponse", response.body().toString())
                response.body()?.let { onResult(it) }
            } else {
                println("Response not successful: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<List<ShareSpaceList>>, t: Throwable) {
            Log.d("APIResponse", "요청 실패")
        }
    })
}

fun createShareSpace(title: String, onResult: (ShareSpaceList) -> Unit) {
    val createShareSpaceRequest = CreateShareSpaceRequest(title)
    val call = apiService.createShareSpace(authorizationHeader, createShareSpaceRequest)

    call.enqueue(object: Callback<ShareSpaceList> {
        override fun onResponse(call: Call<ShareSpaceList>, response: Response<ShareSpaceList>) {
            if (response.isSuccessful) {
                response.body()?.let {
                    shareSpace -> Log.d("APIResponse", shareSpace.toString())
                    onResult(shareSpace)
                }
            } else {
                println("Response not successful: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<ShareSpaceList>, t: Throwable) {
            Log.d("APIResponse", "요청 실패")
        }
    })
}

fun participateShareSpace(spaceId: String, onSuccess: () -> Unit) {
    val participateShareSpaceRequest = ParticipateShareSpaceRequest(spaceId)
    val call = apiService.participateShareSpace(authorizationHeader, participateShareSpaceRequest)

    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("APIResponse", "참가 성공")
                onSuccess()
            } else {
                Log.d("APIResponse", "요청 실패")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.d("APIResponse", "기타 오류로 요청 실패")
        }
    })
}

fun leaveShareSpace(spaceId: String, onSuccess: () -> Unit) {
    val call = apiService.leaveShareSpace(authorizationHeader, spaceId)

    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("APIResponse", "나가기 성공")
                onSuccess()
            } else {
                Log.d("APIResponse", "요청 실패")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.d("APIResponse", "기타 이유로 요청 실패")
        }
    })
}

fun renameShareSpace(spaceId: String, title: String) {
    val renameShareSpaceRequest = RenameShareSpaceRequest(spaceId, title)
    val call = apiService.renameShareSpace(authorizationHeader,renameShareSpaceRequest)

    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("RenameResponse", "이름 변경")
            } else {
                Log.d("RenameResponse", response.toString())
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.d("RenameResponse", "기타 이유로 요청 실패")
        }
    })
}

fun getMarkdown(spaceId: String, onResult: (MarkdownDataResponse) -> Unit) {
    val call = apiService.getMarkDown(authorizationHeader, spaceId)

    call.enqueue(object: Callback<MarkdownDataResponse> {
        override fun onResponse(
            call: Call<MarkdownDataResponse>,
            response: Response<MarkdownDataResponse>
        ) {
            if (response.isSuccessful) {
                Log.d("APIResponse", "요청 성공")
                response.body()?.let { onResult(it) }
            } else {
                Log.d("APIResponse", "요청 실패")
                onResult(MarkdownDataResponse(""))
            }
        }

        override fun onFailure(call: Call<MarkdownDataResponse>, t: Throwable) {
            Log.d("APIResponse", "기타 이유로 요청 실패")
        }
    })
}

fun patchMarkdown(spaceId: String, data: String) {
    val patchRequest = MarkdownDataPatchRequest(spaceId, data)
    val call = apiService.patchMarkDown(authorizationHeader, patchRequest)

    call.enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("APIResponse", "수정 완료")
            } else {
                Log.d("APIRespones", "수정 실패")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.d("APIResponse", "기타 이유로 요청 실패")
        }
    })
}


//fun shareWithKakao() {
//    if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
//        // 카카오톡으로 카카오톡 공유 가능
//        ShareClient.instance.shareDefault(context, defaultFeed) { sharingResult, error ->
//            if (error != null) {
//                Log.e(TAG, "카카오톡 공유 실패", error)
//            }
//            else if (sharingResult != null) {
//                Log.d(TAG, "카카오톡 공유 성공 ${sharingResult.intent}")
//                startActivity(sharingResult.intent)
//
//                // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
//                Log.w(TAG, "Warning Msg: ${sharingResult.warningMsg}")
//                Log.w(TAG, "Argument Msg: ${sharingResult.argumentMsg}")
//            }
//        }
//    }
//}