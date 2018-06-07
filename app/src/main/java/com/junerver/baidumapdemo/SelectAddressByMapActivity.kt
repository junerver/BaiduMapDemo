package com.junerver.baidumapdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.geocode.*
import com.baidu.mapapi.search.sug.SuggestionResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import kotlinx.android.synthetic.main.activity_select_address_by_map.*
import java.util.*

class SelectAddressByMapActivity : AppCompatActivity() {

    private val REQUEST_CODE_CITY = 999
    private lateinit var mLocClient: LocationClient
    private lateinit var geoCoder: GeoCoder
    private var isFirstLoc = true
    private lateinit var locationLatLng: LatLng
    private lateinit var mSuggestionSearch: SuggestionSearch
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mCurrentMode: MyLocationConfiguration.LocationMode
    private lateinit var sugAdapter: ArrayAdapter<String> //输入搜索内容显示的提示
    private lateinit var mSelectCity: String
    private var mSuggestionInfos: MutableList<SuggestionResult.SuggestionInfo> = ArrayList()// 搜索结果列表
    private var acStateIsMap = true//当前页面是地图还是搜索
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(applicationContext)
        setContentView(R.layout.activity_select_address_by_map)
        mContext = this
        mImgBack.setOnClickListener { v ->
            if (!acStateIsMap) {
                mLlMap.visibility = View.VISIBLE
                mLlSearch.visibility = View.GONE
                acStateIsMap = true
            } else {
                this.setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        mEtJiedaoName.setOnClickListener { v ->
            if (acStateIsMap) {
                mLlMap.visibility = View.GONE
                mLlSearch.visibility = View.VISIBLE
                acStateIsMap = false
            }
        }
        mTvSelectedCity.setOnClickListener { v ->
            //此处打开城市列表页面
        }
        initMap()
    }

    private fun initMap() {
        mBaiduMap = mMap.map
        val mapStatus = MapStatus.Builder().zoom(15f).build()
        val mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus)
        mBaiduMap.setMapStatus(mMapStatusUpdate)
        // 地图状态改变相关监听
        mBaiduMap.setOnMapStatusChangeListener(object : BaiduMap.OnMapStatusChangeListener {
            override fun onMapStatusChangeStart(p0: MapStatus?) {}

            override fun onMapStatusChangeStart(p0: MapStatus?, p1: Int) {}

            override fun onMapStatusChange(p0: MapStatus?) {}

            override fun onMapStatusChangeFinish(p0: MapStatus?) {
                // 获取地图最后状态改变的中心点
                val cenpt = p0!!.target
                //将中心点坐标转化为具体位置信息，当转化成功后调用onGetReverseGeoCodeResult()方法
                geoCoder.reverseGeoCode(ReverseGeoCodeOption().location(cenpt))
            }
        })
        // 开启定位图层
        mBaiduMap.isMyLocationEnabled = true
        // 定位图层显示方式
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL
        mBaiduMap.setMyLocationConfigeration(MyLocationConfiguration(mCurrentMode, true, null))
        mLocClient = LocationClient(this)

        // 创建GeoCoder实例对象
        geoCoder = GeoCoder.newInstance()
        geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(p0: GeoCodeResult?) {}

            override fun onGetReverseGeoCodeResult(p0: ReverseGeoCodeResult?) {
                val poiInfos = p0!!.poiList
                if (poiInfos != null) {
                    val poiAdapter = PoiAdapter(mContext, poiInfos)
                    mLvResult.adapter = poiAdapter
                    mLvResult.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        val poiInfo = poiInfos[position]
                        val intent = Intent()
                        intent.putExtra("address", poiInfo.name)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        })

        // 初始化搜索模块，注册搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance()
        mSuggestionSearch.setOnGetSuggestionResultListener { suggestionResult ->
            if (suggestionResult == null || suggestionResult.allSuggestions == null) {
                return@setOnGetSuggestionResultListener
            }
            mSuggestionInfos.clear()
            sugAdapter.clear()
            val suggestionInfoList = suggestionResult.allSuggestions
            if (suggestionInfoList != null) {
                for (info in suggestionInfoList) {
                    if (info.pt != null) {
                        mSuggestionInfos.add(info)
                        sugAdapter.add(info.district + info.key)
                    }
                }
            }
            sugAdapter.notifyDataSetChanged()
        }
        // 注册定位监听
        mLocClient.registerLocationListener { bdLocation ->
            // 如果bdLocation为空或mapView销毁后不再处理新数据接收的位置
            if (bdLocation == null || mBaiduMap == null) {
                return@registerLocationListener
            }
            val data = MyLocationData.Builder()// 定位数据
                    .accuracy(bdLocation.radius)// 定位精度bdLocation.getRadius()
                    .direction(bdLocation.direction)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(bdLocation.latitude)// 经度
                    .longitude(bdLocation.longitude)// 纬度
                    .build()// 构建
            mBaiduMap.setMyLocationData(data)// 设置定位数据
            // 是否是第一次定位
            if (isFirstLoc) {
                isFirstLoc = false
                val ll = LatLng(bdLocation.latitude, bdLocation.longitude)
                val msu = MapStatusUpdateFactory.newLatLngZoom(ll, 18f)
                mBaiduMap.animateMapStatus(msu)
                locationLatLng = LatLng(bdLocation.latitude, bdLocation.longitude)
                // 获取城市，待会用于POISearch
                mSelectCity = bdLocation.city
                mTvSelectedCity.text = mSelectCity
                // 发起反地理编码请求(经纬度->地址信息)
                val reverseGeoCodeOption = ReverseGeoCodeOption()
                // 设置反地理编码位置坐标
                reverseGeoCodeOption.location(locationLatLng)
                geoCoder.reverseGeoCode(reverseGeoCodeOption)
            }
        }
        // 定位选项
        val option = LocationClientOption()
        option.setCoorType("bd09ll")
        option.setIsNeedAddress(true)
        option.setIsNeedLocationDescribe(true)
        option.setIsNeedLocationPoiList(true)
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.isOpenGps = true
        option.setScanSpan(1000)
        mLocClient.locOption = option
        mLocClient.start()

        sugAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        mLvSearch.adapter = sugAdapter
        mLvSearch.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val info = mSuggestionInfos[i]
            val intent = Intent()
            intent.putExtra("address", info.district + info.key)
            setResult(RESULT_OK, intent)
            finish()
        }

        mEtJiedaoName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {}

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                if (cs.isEmpty()) {
                    return
                }
                mSuggestionSearch.requestSuggestion(SuggestionSearchOption()
                        .citylimit(true)
                        .keyword(cs.toString())
                        .city(mSelectCity))
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mMap.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMap.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocClient.stop()
        mBaiduMap.setMyLocationEnabled(false)
        mMap.onDestroy()
        if (geoCoder != null) {
            geoCoder.destroy()
        }
        mSuggestionSearch.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        //此处处理城市选择
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CITY && resultCode == Activity.RESULT_OK) {
            mSelectCity = data.getStringExtra("city")
            mTvSelectedCity.text = mSelectCity
            mSuggestionInfos.clear()
            sugAdapter.clear()
            sugAdapter.notifyDataSetChanged()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (!acStateIsMap) {
                mLlMap.visibility = View.VISIBLE
                mLlSearch.visibility = View.GONE
                acStateIsMap = true
                return false
            } else {
                this.setResult(Activity.RESULT_CANCELED)
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 拖动检索提示
     */
    internal inner class PoiAdapter(private val context: Context, private val pois: List<PoiInfo>) : BaseAdapter() {
        private var linearLayout: LinearLayout? = null

        override fun getCount(): Int {
            return pois.size
        }

        override fun getItem(position: Int): Any {
            return pois[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            var holder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.locationpois_item, null)
                linearLayout = convertView!!.findViewById(R.id.locationpois_linearlayout) as LinearLayout
                holder = ViewHolder(convertView!!)
                convertView!!.tag = holder
            } else {
                holder = convertView!!.tag as ViewHolder
            }
            if (position == 0) {
                holder.iv_gps.setImageDrawable(resources.getDrawable(R.drawable.gps_orange))
                holder.locationpoi_name.setTextColor(Color.parseColor("#FF9D06"))
                holder.locationpoi_address.setTextColor(Color.parseColor("#FF9D06"))
            } else {
                holder.iv_gps.setImageDrawable(resources.getDrawable(R.drawable.gps_grey))
                holder.locationpoi_name.setTextColor(Color.parseColor("#4A4A4A"))
                holder.locationpoi_address.setTextColor(Color.parseColor("#7b7b7b"))
            }
            val poiInfo = pois[position]
            holder.locationpoi_name.text = poiInfo.name
            holder.locationpoi_address.text = poiInfo.address
            return convertView!!
        }


        internal inner class ViewHolder(view: View) {
            var iv_gps: ImageView = view.findViewById(R.id.iv_gps) as ImageView
            var locationpoi_name: TextView = view.findViewById(R.id.locationpois_name) as TextView
            var locationpoi_address: TextView = view.findViewById(R.id.locationpois_address) as TextView


        }
    }

}
