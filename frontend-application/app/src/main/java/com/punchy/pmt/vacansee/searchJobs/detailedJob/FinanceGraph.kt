package com.punchy.pmt.vacansee.searchJobs.detailedJob

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.punchy.pmt.vacansee.searchJobs.httpRequests.FinanceData
import io.data2viz.charts.chart.*
import io.data2viz.charts.chart.mark.area
import io.data2viz.geom.Size
import io.data2viz.viz.VizContainerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinanceGraph.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinanceGraph : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VizContainerView {
        // Inflate the layout for this fragment
        //            inflater.inflate(R.layout.fragment_finance_graph, container, false)

        return FinanceChart(500.0, financeData, context)
    }


    class FinanceChart(graphSize: Double, financeData: List<FinanceData>, context: Context?) :
        VizContainerView(context!!) {
        private val internalGraphSize = graphSize
        private val chart: Chart<FinanceData> = chart(financeData) {
            size = Size(graphSize, graphSize)
            title = "Stocks history"

            // Create a discrete dimension for the year of the census
            val year = discrete({ domain.date })

            // Create a continuous numeric dimension for the population
            val population = quantitative({ domain.sharePrice }) {
                name = "Price of share"
            }

            // Using a discrete dimension for the X-axis and a continuous one for the Y-axis
            area(year, population)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            chart.size = Size(internalGraphSize, internalGraphSize * h / w)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinanceGraph.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinanceGraph().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}