package com.wavesplatform.wallet.v2.ui.home

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pers.victor.ext.toast
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor() : BasePresenter<MainView>() {
    var checkedAboutFundsOnDevice = false
    var checkedAboutBackup = false
    var checkedAboutTerms = false

    fun isAllCheckedToStart(): Boolean {
        return checkedAboutBackup && checkedAboutFundsOnDevice && checkedAboutTerms
    }

    fun loadAliases() {
        addSubscription(apiDataManager.loadAliases()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                }))
    }

    fun loadSpamAssets() {
        addSubscription(spamDataManager.loadSpamAssets()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    toast("success")
                }))
    }
}
