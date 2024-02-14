package com.example.Toyama_Touhoku_Tana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Configuration extends AppCompatActivity {

    private Button master_download_btn;
    private Button h_master_download_btn;
    private Button db_create_btn;

    private Button delete_btn_02;

    //----------DB 接続用 ----------
    private TestOpenHelper helper;
    private SQLiteDatabase db;

    private WebView webview;


    private ImageButton back_btn_002;

    //---------- 再起動設定
    private Context context;
    private int waitperiod;


    /***
     *  JIM 社内　作業用　パス
     */
    private static final String [] JIM_Test = {
            "CSV_取得ディレクトリパス/get_csv/RZMF.csv",  // 0
            "CSV_取得ディレクトリパス/SHMF.csv",  // 1
            "CSV_取得ディレクトリパス/get_csv/SOMF.csv",  // 2
            "CSV_取得ディレクトリパス/get_csv/TNMF.csv"  // 3
    };

    /**
     *  外山産業　東北　本番
     */
    private static final String [] Toyama_Touhoku_Url = {
            "CSV_取得ディレクトリパス/get_csv/RZMF.csv",  // 0
            "CSV_取得ディレクトリパス/get_csv/SHMF.csv",  // 1
            "CSV_取得ディレクトリパス/get_csv/SOMF.csv",  // 2
            "CSV_取得ディレクトリパス/get_csv/TNMF.csv"   // 3
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        //-------- 再起動設定
        context = getApplicationContext();
        waitperiod = 3000;


        // コンポーネント　接続
        finde();

        webview = new WebView(this);

        // Main activity　へ　戻るボタン　ヘッダー
        back_btn_002.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 戻る
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);

                finish();

            }
        });




        // 最新　マスター　受信　ボタン
        master_download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // === データベース作成
                Create_DB();

                try {
                    Thread.sleep(600); // 0.6秒 間だけ処理を止める

                    Toast.makeText(getApplicationContext(), "ダウンロード 開始します。", Toast.LENGTH_LONG).show();

                    // TNMF.csv インポート
                    TNMF_table_GET();

                    Toast.makeText(getApplicationContext(), "ボタンの色が変わるまでお待ちください。", Toast.LENGTH_LONG).show();

                    // SOMF.csv インポート
                    SOMF_TB_GET();
                    // RZMF.csv インポート
                    RZMF_table_GET();
                    // SHMF ダウンロード
                    SHMF_TB_GET();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                webview.reload();

                return;
            }
        }); //---------------- マスター　CSV download END



        //------------------ 設定　削除　ボタン ------------------

        delete_btn_02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //--------------- アラートダイヤログ　削除　設定 ---------------//
                // タイトル
                TextView titleView;
                titleView = new TextView(Configuration.this);
                titleView.setText("取得ファイルを全て削除する");
                titleView.setTextSize(20);
                titleView.setTextColor(Color.WHITE);
                titleView.setBackgroundColor(getResources().getColor(R.color.back_color_03));
                titleView.setPadding(20, 20, 20, 20);
                titleView.setGravity(Gravity.CENTER);


                //-------------- アラートログの表示 開始 -------------- //
                AlertDialog.Builder bilder = new AlertDialog.Builder(Configuration.this);

                // ダイアログの項目
                bilder.setCustomTitle(titleView);
                //  bilder.setTitle("削除");
                bilder.setMessage("ダウンロードした設定ファイルを全て削除しますか？");

                //------- OK の時の処理 ----------//
                bilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                        SQLiteDatabase db = helper.getReadableDatabase();

                        // TestDB テーブル一覧　削除
                        /**
                         *  === 東北用　テーブル削除　処理
                         */
                        db.delete("SHMF_table",null,null);
                        db.delete("TNMF_table",null,null);
                        db.delete("SOMF_table",null,null);
                        db.delete("RZMF_table",null,null);


                        deleteDatabase("TestDB.db");   // マスター用 DB 削除
                        deleteDatabase("Send.db");   // 送信用　DB 削除

                        toastMake("設定初期化　完了しました。", 0, -200);

                        //------------ 削除　処理　END　----------------

                    }
                });

                bilder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        return;
                    }
                });


                // ダイアログの表示
                AlertDialog dialog = bilder.create();
                dialog.show();

            }
        });

    }

    // コンポーネント　接続
    private void finde() {
        // ヘッダーボタン
        back_btn_002 = (ImageButton) findViewById(R.id.back_btn_002);
        master_download_btn =(MaterialButton) findViewById(R.id.master_download_btn);
        delete_btn_02 = (MaterialButton) findViewById(R.id.delete_btn_02);
    }



    //------------ CSV (SHMF.csv) ダウンロード & DB 挿入処理 テーブル名 => SHMF_table
    private void SHMF_TB_GET() {

        //------------ CSV (SHMF.csv) ダウンロード & DB 挿入処理 テーブル名 => SHMF_table

        new AsyncTask<Void, Void, String>() {
            @Override

            protected String doInBackground(Void... params) {

                String result = null;
                Request request = new Request.Builder()
                        .url(JIM_Test[1]) // 本番用 URL
                      //  .url(Toyama_Touhoku_Url[1]) // 本番用 URL
                        .get()
                        .build();

                // OKHttp の　クライアントオブジェクトを作る
                OkHttpClient client = new OkHttpClient();

                // リクエストして結果を受け取る
                try {

                    Response response = client.newCall(request).execute();
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {

                TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                //----------- 削除処理
                // トランザクション　開始
                db.beginTransaction();
                try {

                    db.delete("SHMF_table", null,null);

                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } finally {
                    //------------- トランザクション　完了
                    db.endTransaction();
                }

                //----------- 削除処理  END -------------->

                String line = null;

                String [] RowDown = new String[5];

                // トランザクション　開始
                db.beginTransaction();

                try {

                    BufferedReader reader = new BufferedReader(new StringReader(result));
                    int count = 1;

                    while ((line = reader.readLine()) != null) {

                        RowDown = line.split(",", -1);

                        // DB 値　格納用　変数
                        ContentValues values = new ContentValues();

                        System.out.println(">>> " + RowDown[0]);
                        System.out.println(">>> " + RowDown[1]);
                        System.out.println(">>> " + RowDown[2]);
                        System.out.println(">>> " + RowDown[3]);
                        System.out.println(">>> " + RowDown[4]);

                        values.put(TestOpenHelper.SHMF_TB_column_01, RowDown[0]);
                        values.put(TestOpenHelper.SHMF_TB_column_02, RowDown[1]);
                        values.put(TestOpenHelper.SHMF_TB_column_03, RowDown[2]);
                        values.put(TestOpenHelper.SHMF_TB_column_04, RowDown[3]);
                        values.put(TestOpenHelper.SHMF_TB_column_05, RowDown[4]);


                        db.insert("SHMF_table",null,values);
                        count++;
                        System.out.println("SHMF_table DB インサート　成功" + count + "件");
                    }

                    //---------- トランザクション　成功
                    db.setTransactionSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {

                    db.endTransaction();
                    db.close();

                    //*********************************** データ受信　後　ボタン　背景変更
                    master_download_btn.setBackgroundColor(Color.parseColor("#CAEA8FB0"));

                    Toast.makeText(getApplicationContext(), "マスターデータ、ダウンロード完了。", Toast.LENGTH_LONG).show();
                }


            } //-------- onPostExecute END


        }.execute();

    } //-------------- Company_code_in END



    //---------------------- CSV 読み込み（TNMF.csv）　メソッド TNMF_table テーブル
    private void TNMF_table_GET() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String result = null;
                Request request = new Request.Builder()
                        .url(JIM_Test[3])
                     //   .url(Toyama_Touhoku_Url[3])
                        .get()
                        .build();

                // OKHttp の　クライアントオブジェクトを作る
                OkHttpClient client = new OkHttpClient();

                // リクエストして　結果を受け取る
                try {

                    Response response = client.newCall(request).execute();

                    // 結果を格納
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return result;

            } //-------------------- doInBackground END

            @Override
            protected void onPostExecute(String result) {

                TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getReadableDatabase();


                //----------- 削除処理 ----------------
                // トランザクション　開始
                db.beginTransaction();
                try {

                    db.delete("TNMF_table", null, null);
                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } finally {
                    //------------- トランザクション　完了
                    db.endTransaction();
                }

                //----------- 削除処理 END -------------->

                String line = null;

                String [] RowsData = new String[2];

                // トランザクション　開始
                db.beginTransaction();

                try {

                    BufferedReader reader = new BufferedReader(new StringReader(result));

                    int count = 1;

                    while((line = reader.readLine()) != null) {

                        RowsData = line.split(",", -1);

                        ContentValues values = new ContentValues();

                        values.put(TestOpenHelper.TNMF_table_column_01,RowsData[0]);  // ログイン ID
                        values.put(TestOpenHelper.TNMF_table_column_02,RowsData[1]);  // 担当者名

                        System.out.println("RowsData[0]:::" + RowsData[0]);
                        System.out.println("RowsData[1]:::" + RowsData[1]);

                        db.insert("TNMF_table", null, values);

                        count++;

                        System.out.println("TNMF_table テーブル DB １行　インサート　成功" + count + "件");

                    }

                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    // トランザクション　終了
                    db.endTransaction();
                    db.close();
                }

            }

        }.execute();


    } //----------- END DB_User_import



    // テーブル => SOMF_TB , CSV => SOMF.csv DB insert 格納
    private void SOMF_TB_GET() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String result = null;
                // リクエスト　作成
                Request request = new Request.Builder()
                     //   .url(Toyama_Touhoku_Url[2]) // 本番用 URL
                        .url(JIM_Test[2]) // 本番用 URL
                        .get()
                        .build();

                //　クライアント　作成
                OkHttpClient client = new OkHttpClient();

                // リクエストして　結果を受け取る
                try {

                    Response response = client.newCall(request).execute();

                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;


            } //---------- END doInBackground

            @Override
            protected void onPostExecute(String result) {

                TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                //----------- 削除処理 ----------------
                // トランザクション　開始
                db.beginTransaction();
                try {

                    db.delete("SOMF_table", null, null);
                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } finally {
                    //------------- トランザクション　完了
                    db.endTransaction();
                }

                //----------- 削除処理 END -------------->

                String line = null;
                String [] RowsData = new String[3];

                // トランザクション　開始
                db.beginTransaction();

                try {

                    BufferedReader reader = new BufferedReader(new StringReader(result));

                    int count = 1;

                    while((line = reader.readLine()) != null) {

                        RowsData = line.split(",", -1);

                        ContentValues values = new ContentValues();

                        values.put(TestOpenHelper.SOMF_c_01_column_01, RowsData[0]); // 倉庫C
                        values.put(TestOpenHelper.SOMF_c_01_column_02, RowsData[1]); // 倉庫名
                        values.put(TestOpenHelper.SOMF_c_01_column_03, RowsData[2]); // 棚卸日

                        db.insert("SOMF_table", null, values);

                        count++;

                        System.out.println("SOMF_table テーブル DB １行　インサート　成功" + count + "件");

                    }

                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } finally {
                    // トランザクション　終了
                    db.endTransaction();
                    db.close();
                }

            }



        }.execute();



    } //-------------- Company_code_in END


    private void RZMF_table_GET() {

        //------------ CSV (SHKB.csv) ダウンロード & DB 挿入処理 テーブル名 => Item_tb

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                // OKHttp の　リクエストオブジェクト　作成
                Request request = new Request.Builder()
                        .url(JIM_Test[0]) // 本番用 URL
                   //     .url(Toyama_Touhoku_Url[0]) // 本番用 URL
                        .get()
                        .build();

                // OKHttp の　クライアントオブジェクトを作る
                OkHttpClient client = new OkHttpClient();

                // リクエストして　結果を受け取る
                try {

                    Response response = client.newCall(request).execute();

                    // 結果を格納
                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                return result;

            } //-------------------- doInBackground END


            @Override
            protected void onPostExecute(String result) {

                TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                //----------- 削除処理 ----------------
                // トランザクション　開始
                db.beginTransaction();
                try {

                    db.delete("RZMF_table", null, null);
                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } finally {
                    //------------- トランザクション　完了
                    db.endTransaction();
                }

                //----------- 削除処理 END -------------->

                String[] RowDown = new String[3];

                String line = null;

                // トランザクション　開始
                db.beginTransaction();

                try {

                    BufferedReader reader = new BufferedReader(new StringReader(result));

                    int count = 1;

                    while ((line = reader.readLine()) != null) {

                        RowDown = line.split(",", -1);

                        ContentValues values = new ContentValues();

                        // DB INSERT 処理  カラム item_tb_c_01, item_tb_c_02
                        values.put(TestOpenHelper.RZMF_c_01_column_01, RowDown[0]); // 商品C
                        values.put(TestOpenHelper.RZMF_c_01_column_02, RowDown[1]); // 倉庫C
                        values.put(TestOpenHelper.RZMF_c_01_column_03, RowDown[2]); // 棚番

                        System.out.println("項目01" + RowDown[0] + "項目02" + RowDown[1] +
                                "項目03" + RowDown[2]);

                        db.insert("RZMF_table", null, values);
                        System.out.println("RZMF_table DB インサート　成功" + count + "件");

                        count++;

                    }

                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();

                } catch (SQLException e) {
                    e.printStackTrace();

                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    // トランザクション　終了
                    db.endTransaction();
                    db.close();



                }

            }


        }.execute();

    } //--------------------- END Item_tb_IN


    // **************** 商品マスター　ダウンロード *********************
    // --------------- CSV インポート SHMF.csv ------------------

    private void Shmf_in() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String result = null;

                //String jim = "CSV_取得ディレクトリパス/get_csv/SHMF.csv"; // 社内
                //String jim = "CSV_取得ディレクトリパス/SHMF.csv"; // 社内 ローカル
                // 本番 :
                 String tm = "CSV_取得ディレクトリパス/get_csv/SHMF.csv";

                // リクエスト　作成  // ****************** 商品マスター *********************
                Request request = new Request.Builder()
                        .url(tm)
                        .get()
                        .build();

                //　クライアント　作成
                OkHttpClient client = new OkHttpClient();

                // リクエストして　結果を受け取る
                try {

                    Response response = client.newCall(request).execute();

                    result = response.body().string();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;


            } //---------- END doInBackground

            @Override
            protected void onPostExecute(String result) {

                TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase db = helper.getReadableDatabase();

                //----------- 削除処理 ----------------
                // トランザクション　開始
                db.beginTransaction();
                try {

                    db.delete(TestOpenHelper.TABLE_NAME, null, null);
                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } finally {
                    //------------- トランザクション　完了
                    db.endTransaction();
                }

                String line = null;
                String[] RowData = new String[9];

                // トランザクション　開始
                db.beginTransaction();

                try {

                    int count = 1;

                    BufferedReader reader = new BufferedReader(new StringReader(result));

                    while ((line = reader.readLine()) != null) {

                        RowData = line.split(",", -1);

                        ContentValues values = new ContentValues();


                        values.put(TestOpenHelper.COLUMN_01, RowData[0]);
                        values.put(TestOpenHelper.COLUMN_02, RowData[1]);
                        values.put(TestOpenHelper.COLUMN_03, RowData[2]);
                        values.put(TestOpenHelper.COLUMN_04, RowData[3]);
                        values.put(TestOpenHelper.COLUMN_05, RowData[4]);
                        values.put(TestOpenHelper.COLUMN_06, RowData[5]);
                        values.put(TestOpenHelper.COLUMN_07, RowData[6]);
                        values.put(TestOpenHelper.COLUMN_08, RowData[7]);
                        values.put(TestOpenHelper.COLUMN_09, RowData[8]);

                        db.insert(TestOpenHelper.TABLE_NAME, null, values);

                        System.out.println("商品マスター テーブル DB １行　インサート　成功" + count + "件");

                        for (int i = 0; i <= 8; i++) {
                            System.out.println("項目" + i + "番" + RowData[i]);
                        }

                    }

                    // トランザクション　成功
                    db.setTransactionSuccessful();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } finally {
                    // トランザクション　終了
                    db.endTransaction();
                    Toast.makeText(getApplicationContext(), "ダウンロード 「100%完了」 しました。", Toast.LENGTH_LONG).show();

                     //******************** ダウンロード後　ボタン　背景色　変更
                    h_master_download_btn.setBackgroundColor(Color.parseColor("#CAEA8FB0"));

                }


            } //----------- END onPostExecute


        }.execute();


    }  // --------------- ********** SHMF.csv インサート処理 ********** 終了 ------------


    @Override
    public void onBackPressed() {
        // メイン画面へ　もどる　

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void toastMake (String message,int x, int y){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);

        // 位置調整
        toast.setGravity(Gravity.CENTER, x, y);
        toast.show();
    }


    private void Create_DB()
    {

        try {
            //---------- データベース作成処理 ---------
            if(helper == null) {
                // DB　の作成
                helper = new TestOpenHelper(getApplicationContext());

            } else {
                //  toastMake("「ヘルパー オブジェクト」 設定は完了しています。", 0, -200);
                return;
            }

            // DB が空なら　作成
            if(db == null) {
                db = helper.getWritableDatabase();
                toastMake("初期セッティング 設定完了", 0, -200);

                //************ ボタンカラー　変更
                db_create_btn.setBackgroundColor(Color.parseColor("#CAEA8FB0"));

            } else {
                // DB が作られていた場合
                //   toastMake("「DB」 設定は完了しています。", 0, -200);
                return;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }


}
