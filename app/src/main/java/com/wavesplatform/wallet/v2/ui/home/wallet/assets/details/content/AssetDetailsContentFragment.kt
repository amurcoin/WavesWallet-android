package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content


import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.color.v
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_asset_details.*
import kotlinx.android.synthetic.main.fragment_asset_details_content.*
import kotlinx.android.synthetic.main.spinner_balance_header.view.*
import pers.victor.ext.*
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class AssetDetailsContentFragment : BaseFragment(), AssetDetailsContentView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetDetailsContentPresenter

    @ProvidePresenter
    fun providePresenter(): AssetDetailsContentPresenter = presenter

    @Inject
    lateinit var historyAdapter: HistoryTransactionPagerAdapter
    var formatter: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")

    override fun configLayoutRes() = R.layout.fragment_asset_details_content

    companion object {
        var BUNDLE_ASSET = "asset"
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.assetBalance = arguments?.getParcelable<AssetBalance>(BUNDLE_ASSET)

        view_pager_transaction_history.adapter = historyAdapter
        view_pager_transaction_history.offscreenPageLimit = 3
        view_pager_transaction_history.clipToPadding = false
        view_pager_transaction_history.setPadding(dp2px(14), 0, dp2px(14), 0);
        view_pager_transaction_history.pageMargin = dp2px(7)

        image_copy_issuer.click {
            text_view_issuer_value.copyToClipboard(it)
        }

        image_copy_id.click {
            text_view_id_value.copyToClipboard(it)
        }

        card_burn.click {
            launchActivity<TokenBurnActivity> { }
        }

        fillInformation(presenter.assetBalance)


        runAsync({
            presenter.loadLastTransactions()
        })
    }

    override fun showLastTransactions(data: MutableList<HistoryItem>) {
        if (data.isNotEmpty()) {
            // configure clickable card
            card_transaction.setCardBackgroundColor(findColor(R.color.white))
            card_transaction.cardElevation = dp2px(2).toFloat()
            relative_transaction.setBackgroundResource(0)
            text_view_history.setTextColor(findColor(R.color.black))
            card_transaction.click {
                launchActivity<HistoryActivity> {
                    val bundle = Bundle().apply {
                        val tabs = arrayListOf(
                                HistoryTab(HistoryTabFragment.all, getString(R.string.history_all)),
                                HistoryTab(HistoryTabFragment.send, getString(R.string.history_sent)),
                                HistoryTab(HistoryTabFragment.received, getString(R.string.history_received)),
                                HistoryTab(HistoryTabFragment.exchanged, getString(R.string.history_exchanged)),
                                HistoryTab(HistoryTabFragment.leased, getString(R.string.history_leased)),
                                HistoryTab(HistoryTabFragment.issued, getString(R.string.history_issued)))
                        putParcelable(HistoryFragment.BUNDLE_ASSET, presenter.assetBalance)
                        putParcelableArrayList(HistoryFragment.BUNDLE_TABS, tabs)
                    }
                    putExtras(bundle)
                }
            }

            historyAdapter.items = data
            historyAdapter.notifyDataSetChanged()
        } else {
            // configure not clickable card
            card_transaction.setCardBackgroundColor(findColor(android.R.color.transparent))
            card_transaction.cardElevation = 0f
            relative_transaction.setBackgroundResource(R.drawable.shape_rect_outline_basic300_transparent)
            text_view_history.setTextColor(findColor(R.color.accent100))
            text_last_transaction_title.text = getString(R.string.asset_details_last_transactions_empty)
            card_transaction.click {}
        }
    }

    private fun fillInformation(assetBalance: AssetBalance?) {
        text_available_balance.text = assetBalance?.getDisplayBalance()
        text_total.text = assetBalance?.getDisplayBalance()

        text_available_balance.makeTextHalfBold()

        text_view_asset_name_value.text = assetBalance?.getName()
        text_reusable_value.text =
                if (assetBalance?.reissuable == true) getString(R.string.asset_details_reissuable)
                else getString(R.string.asset_details_not_reissuable)
        text_description.text =
                if (assetBalance?.issueTransaction?.description.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.description
        text_view_issuer_value.text =
                if (assetBalance?.issueTransaction?.sender.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.sender
        text_view_id_value.text =
                if (assetBalance?.issueTransaction?.assetId.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.assetId
        text_issue_date_value.text = getString(R.string.common_dash)
        assetBalance?.issueTransaction?.timestamp.notNull {
            text_issue_date_value.text = formatter.format(Date(it))
        }

        // hide token burn if WAVES
        card_burn.goneIf { assetBalance?.isWaves() == true }

        // configure view if SPAM
        linear_last_transactions.goneIf { assetBalance?.isSpam == true }
        linear_blocked_transfer_buttons.visiableIf { assetBalance?.isSpam == true }
        linear_transfer_buttons.goneIf { assetBalance?.isSpam == true }

        // TODO: configure view when will be balance
        frame_total.goneIf { text_total.text.toString().trim() == text_available_balance.text.toString().trim() }
        frame_leased.gone()
        frame_in_order.gone()
    }
}
