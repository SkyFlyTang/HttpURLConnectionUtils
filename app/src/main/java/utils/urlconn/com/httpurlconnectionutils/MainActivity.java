package utils.urlconn.com.httpurlconnectionutils;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.http.utils.FileBinary;
import com.http.utils.HttpUrlUtils;
import com.http.utils.OnResponseListener;
import com.http.utils.Request;
import com.http.utils.RequestMethod;
import com.http.utils.Response;
import com.http.utils.StringRequest;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mGetBtn;
    private Button mPostBtn;
    private Button mUploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mGetBtn = findViewById(R.id.btn_get);
        mGetBtn.setOnClickListener(this);
        mPostBtn = findViewById(R.id.btn_post);
        mPostBtn.setOnClickListener(this);
        mUploadBtn = findViewById(R.id.btn_upload);
        mUploadBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get:
                // TODO 18/04/09
                testGet();
                break;
            case R.id.btn_post:
                // TODO 18/04/09
                testPost();
                break;
            case R.id.btn_upload:
                // TODO 18/04/09
                testUpload();
                break;
            default:
                break;
        }
    }

    private void testGet() {

        StringRequest request = HttpUrlUtils.createStringRequest("http://192.168.1.3:8080/test", RequestMethod.GET);
        HttpUrlUtils.INSTANCE().execute(0, request, new OnResponseListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                toastShowLong("response:" + response.get() + " what:" + what);
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                toastShowLong("error：" + response.getError());
            }
        });
    }

    private void testPost() {
        Request request = HttpUrlUtils.createStringRequest("http://192.168.1.3:8080/login", RequestMethod.POST)
                .addFromBoy("username", "123")
                .addFromBoy("password", "123");
        HttpUrlUtils.INSTANCE().execute(0, request, new OnResponseListener() {
            @Override
            public void onSucceed(int what, Response response) {
                toastShowLong("response:" + response.get() + " what:" + what);
            }

            @Override
            public void onFailed(int what, Response response) {
                toastShowLong("error：" + response.getError());
            }
        });


    }

    private void testUpload() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "timg.jpg");

        Request request = HttpUrlUtils.createStringRequest("http://192.168.1.3:8080/upload", RequestMethod.POST)
                .addBinaryFormsBody("image0", new FileBinary(file));
        HttpUrlUtils.INSTANCE().execute(0, request, new OnResponseListener() {
            @Override
            public void onSucceed(int what, Response response) {
                toastShowLong("response:" + response.get() + " what:" + what);
            }

            @Override
            public void onFailed(int what, Response response) {
                toastShowLong("error：" + response.getError());
            }
        });


    }

    private void toastShowLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }
}
