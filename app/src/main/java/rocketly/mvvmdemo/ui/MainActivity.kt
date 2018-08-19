package rocketly.mvvmdemo.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import me.drakeet.multitype.MultiTypeAdapter
import rocketly.mvvmdemo.R
import rocketly.mvvmdemo.databinding.ActivityMainBinding
import rocketly.mvvmdemo.model.Basic
import rocketly.mvvmdemo.utils.funcextend.observe
import rocketly.mvvmdemo.viewmodel.HotCityListVM

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            vm = ViewModelProviders.of(this@MainActivity).get(HotCityListVM::class.java)
        }
        initView()
        initListener()
    }

    private fun initView() {
        binding.srl.setColorSchemeResources(R.color.colorPrimary)
        binding.rv.adapter = MultiTypeAdapter(mutableListOf<Any>()).apply {
            register(Basic::class.java, HotCityListViewBinder(binding.vm))
        }
        binding.rv.layoutManager = LinearLayoutManager(this)
    }

    private fun initListener() {
        binding.vm?.apply {
            isRefreshing.observe(this@MainActivity) {
                it ?: return@observe
                binding.srl.isRefreshing = it
            }
            hotCityItemEvent.observe(this@MainActivity) {
                it ?: return@observe
                Toast.makeText(this@MainActivity, "点击了:$it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.vm?.onFirstLoad()
    }

}
