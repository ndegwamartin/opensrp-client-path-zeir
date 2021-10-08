package org.smartregister.path.reporting.monthly.indicator.summary

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_report_indicators_summary.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.smartregister.path.R
import org.smartregister.path.reporting.common.ReportingUtils
import org.smartregister.path.reporting.common.ReportingUtils.dateFormatter
import org.smartregister.path.reporting.common.showProgressDialog
import org.smartregister.path.reporting.common.sortIndicators
import org.smartregister.path.reporting.monthly.domain.MonthlyTally
import org.smartregister.path.reporting.monthly.indicator.ReportIndicatorsViewModel

/**
 * A [Fragment] subclass used to display list of report indicators
 */
class ReportIndicatorsSummaryFragment : Fragment() {

    private lateinit var progressDialog: AlertDialog

    private val reportIndicatorsRecyclerAdapter = ReportIndicatorsRecyclerAdapter()

    private val reportIndicatorsViewModel by activityViewModels<ReportIndicatorsViewModel>()
    { ReportingUtils.createFor(ReportIndicatorsViewModel()) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_report_indicators_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressDialog = requireContext().showProgressDialog(
                show = true,
                title = getString(R.string.loading_monthly_reports_title),
                message = getString(R.string.loading_monthly_reports_message))
        indicatorsRecyclerView.apply {
            adapter = reportIndicatorsRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        reportIndicatorsViewModel.monthlyTalliesMap.value?.let { displayIndicators(it) }
    }

    private fun displayIndicators(monthlyTallies: Map<String, MonthlyTally>) {
        lifecycleScope.launch(Dispatchers.Main) {
            val sortedIndicators = monthlyTallies.values.toList().sortIndicators()
            val groupedTallies: Map<String, List<MonthlyTally>> = sortedIndicators.groupBy { it.grouping }
            reportIndicatorsRecyclerAdapter.apply {
                if (groupedTallies.isNotEmpty()) {
                    val firstMonthlyTally: MonthlyTally = groupedTallies.values.first()[0]
                    val submittedBy = requireContext().getString(R.string.submitted_by_,
                            dateFormatter("dd/MM/yyyy").format(firstMonthlyTally.dateSent!!), firstMonthlyTally.providerId)
                    submittedByTextView.text = submittedBy
                    submittedByTextView.typeface = Typeface.DEFAULT_BOLD
                    reportIndicators = groupedTallies.toList()
                }
            }
            progressDialog.dismiss()
        }
    }
}