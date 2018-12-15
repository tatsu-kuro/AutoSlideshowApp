package jp.techacademy.kuroda.tatsuaki.autoslideshowapp
import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    var idArray = arrayListOf<Long>()
    var idNumber:Int = 0
    var playingF  = false
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
        back_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
        playstop_button.setOnClickListener(this)
    }
    override fun onClick(v: View) {
//        if(idArray.count()==0) {
//            Snackbar.make(v, "画像がありません", Snackbar.LENGTH_INDEFINITE)
//                .setAction("OK") {
//                    Log.d("UI_PARTS", "Snackbarをタップした")
//                }.show()
//            return
//        }
        if(idArray.count()==0){
            return
        }
       when(v.id){
            R.id.back_button -> showBack()//textView.text = editText.text.toString()
            R.id.playstop_button -> playStop()
            R.id.next_button -> showNext()
        }
    }
    fun showBack(){
        idNumber -= 1
        if (idNumber<0){
            idNumber=idArray.count()-1
        }
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idArray[idNumber])
        imageView.setImageURI(imageUri)

    }
    fun playStop(){
        if(playingF){
            playingF=false
            if (mTimer != null){
                mTimer!!.cancel()
                mTimer = null
            }

            playstop_button.text="再生"
            back_button.setEnabled(true);
            next_button.setEnabled(true);

        }else{
            playingF=true
            playstop_button.text="停止"

            back_button.setEnabled(false);
            next_button.setEnabled(false);

            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        showNext()
                    }
                }
            }, 2000, 2000)

        }
    }
    fun showNext() {
        idNumber += 1
        if (idNumber == idArray.count()) {
            idNumber = 0
        }
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idArray[idNumber])
        imageView.setImageURI(imageUri)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }
    private fun getContentsInfo() {
        // 画像の情報を取得する
        if (idArray.isEmpty() == false) {
            idArray.clear()
        }
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                idArray.add(id)
                Log.d("ANDROID", "URI : " + imageUri.toString())
            } while (cursor.moveToNext())
        }
        Log.d("URI**",idArray.count().toString())
        cursor.close()
        if (idArray.isEmpty() == false) {
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idArray[0])
            imageView.setImageURI(imageUri)
        }
    }
}