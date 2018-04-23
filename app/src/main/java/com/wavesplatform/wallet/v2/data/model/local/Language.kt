package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.google.gson.annotations.SerializedName


/**
 * Created by anonymous on 16.12.17.
 */

data class Language(
        @DrawableRes var image: Int,
        @StringRes var title: Int,
        var checked: Boolean
)