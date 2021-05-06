package com.punchy.pmt.vacansee.searchJobs.detailedJob

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.punchy.pmt.vacansee.R
import io.data2viz.charts.chart.Chart
import io.data2viz.charts.chart.chart
import io.data2viz.charts.chart.discrete
import io.data2viz.charts.chart.mark.area
import io.data2viz.charts.chart.quantitative
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): VizContainerView {
        // Inflate the layout for this fragment
        val rootView = FinanceChart(context)
//            inflater.inflate(R.layout.fragment_finance_graph, container, false)

        return rootView
    }

    data class PopCount(val year: Int, val population: Double)


    class FinanceChart(context: Context?) : VizContainerView(context!!) {

        private val chart: Chart<PopCount> = chart(listOf(
            PopCount(1851, 2.436),
            PopCount(1861, 3.23),
            PopCount(1871, 3.689),
            PopCount(1881, 4.325),
            PopCount(1891, 4.833),
            PopCount(1901, 5.371),
            PopCount(1911, 7.207),
            PopCount(1921, 8.788),
            PopCount(1931, 10.377),
            PopCount(1941, 11.507),
            PopCount(1951, 13.648),
            PopCount(1961, 17.78),
            PopCount(1971, 21.046),
            PopCount(1981, 23.774),
            PopCount(1991, 26.429),
            PopCount(2001, 30.007)
        )) {
            size = Size(500.0, 500.0)
            title = "Population of Canada 1851–2001 (Statistics Canada)"

            // Create a discrete dimension for the year of the census
            val year = discrete({ domain.year })

            // Create a continuous numeric dimension for the population
            val population = quantitative({ domain.population }) {
                name = "Population of Canada (in millions)"
            }

            // Using a discrete dimension for the X-axis and a continuous one for the Y-axis
            area(year, population)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            chart.size = Size(500.0, 500.0 * h / w)
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
        @JvmStatic fun newInstance(param1: String, param2: String) =
                FinanceGraph().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}