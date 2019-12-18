package com.codingblocks.cbonlineapp.dashboard.doubts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.codingblocks.cbonlineapp.database.models.DoubtsModel
import com.codingblocks.cbonlineapp.util.extensions.runIO
import com.codingblocks.onlineapi.ResultWrapper
import com.codingblocks.onlineapi.fetchError

class DashboardDoubtsViewModel(private val repo: DashboardDoubtsRepository) : ViewModel() {

    var listDoubtsResponse: LiveData<List<DoubtsModel>> = MutableLiveData()
    var errorLiveData: MutableLiveData<String> = MutableLiveData()
    var nextOffSet: MutableLiveData<Int> = MutableLiveData(-1)
    var prevOffSet: MutableLiveData<Int> = MutableLiveData(-1)
    var barMessage: MutableLiveData<String> = MutableLiveData()
    var courseId: MutableLiveData<String> = MutableLiveData("44872")

    init {
        listDoubtsResponse = Transformations.switchMap(courseId) {
            repo.getDoubtsByCourseRun(it)
        }
    }

    fun fetchDoubts() {
        runIO {
            when (val response = repo.fetchDoubtsByCourseRun()) {
                is ResultWrapper.GenericError -> setError(response.error)
                is ResultWrapper.Success -> {
                    if (response.value.isSuccessful)
                        response.value.body()?.let { repo.insertDoubts(it) }
                    else {
                        setError(fetchError(response.value.code()))
                    }
                }
            }
        }
    }

    private fun setError(error: String) {
        errorLiveData.postValue(error)
    }

    fun resolveDoubt(doubt: DoubtsModel) {
        runIO {
            when (val response = repo.resolveDoubt(doubt)) {
                is ResultWrapper.GenericError -> setError(response.error)
                is ResultWrapper.Success -> {
                    if (response.value.isSuccessful) {
                        fetchDoubts()
                        barMessage.postValue(response.value.body()?.status)
                    } else {
                        setError(fetchError(response.value.code()))
                    }
                }
            }
        }
    }

    fun getDoubt(doubtId: String): LiveData<DoubtsModel> {
        fetchComments(doubtId)
        return repo.getDoubtById(doubtId)
    }

    fun getComments(doubtId: String) = repo.getCommentsById(doubtId)

    private fun fetchComments(doubtId: String) {
        runIO {
            when (val response = repo.fetchCommentsByDoubtId(doubtId)) {
                is ResultWrapper.GenericError -> setError(response.error)
                is ResultWrapper.Success -> {
                    if (response.value.isSuccessful) {
                        response.value.body()?.let { repo.insertComments(it) }
                    } else {
                        setError(fetchError(response.value.code()))
                    }
                }
            }
        }
    }


}