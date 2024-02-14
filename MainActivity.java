package com.example.Toyama_Touhoku_Tana;

import static android.content.ContentValues.TAG;
import static android.text.InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // ----------DB 接続用 ----------
    private TestOpenHelper helper;
    private SQLiteDatabase db;

    private Common common; // グローバル変数を扱うクラス

    // ユーザーテキスト 入力用
    private EditText user_input;

    // アカウント名 表示用 テキストビュー
    private TextView user_view;

    // ------------- アセッツマネージャー --------//
    private AssetManager ass;
    private AssetManager User_as;
    private AssetManager Bu_as;

    // ------------- コンテンツ バリュー ---------//
    private ContentValues value;

    // ---- employee テーブル 取得用 -------//
    private String employee_tb_c_04_num;

    // ------------- イベント ボタン -----------//
    private Button account_put_btn; // アカウント 確定ボタン
    private String g_account; // アカウント 番号 格納用

    private Button setting_01; // DB 作成
    private Button setting_02; // CSV インポート
    private MaterialButton kousin_w; // 最新ボタン

    // アカウント名 格納用
    private String g_account_02;

    private String send_account;

    private ProgressBar progressBar;

    private int val;

    private String[] Row_tnmf = null;

    // ------------- 判別 フラグ
    private int Ac_Flg;
    private int db_Flg;

    private int Item_tb_Flg;

    // ------------- 担当コード 格納
    private ArrayList<String> arr_col = new ArrayList<>();
    private HashMap<String, String> TNMF_item_User = new HashMap<>();

    // ------------- btn
    private Button master_btn;

    // ------------- 効果音 用
    SoundPool soundPool; // 効果音を鳴らす本体（コンポ）
    int mp3a; // 効果音データ（mp3）

    private Button db_delete_btn;

    private WebView webView;

    // -------- トースト用 メッセージ ----------//
    private String str_01 = "「DB設定」が完了しました。　「ファイル受信」に進んでください。";

    // -------- home キー 処理
    // private HomeButtonReceive homeButtonReceiver;

    private Context context;
    private int waitperiod;

    private String Account_Name, Account_Id;

    // 端末 ID表示
    private TextView t_id;
    private String Get_ID;
    // private MaterialButton mozi_c_btn;

    // パーミッションのリクエストコード
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("============ 外山工業 東北 Tana　開始 ==============");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // グローバル変数を扱うクラスを取得する
        common = (Common) getApplication();

        context = getApplicationContext();
        waitperiod = 500; // 5sec

        // グローバル変数の初期化
        common.g_flg = 0;

        // ユーザーテキスト 入力用 エディットテキスト
        user_input = (EditText) findViewById(R.id.user_input);
        // ------- エディットテキスト インプットタイプ設定 , TYPE_TEXT_FLAG_AUTO_COMPLETE 自動補完
        user_input.setInputType(InputType.TYPE_CLASS_NUMBER | TYPE_TEXT_FLAG_AUTO_COMPLETE);
        // アカウント名 表示用 テキストビュー
        user_view = (TextView) findViewById(R.id.user_view);

        // コンポーネント 初期化
        init();

        g_account = user_input.getText().toString();

        // ******** 端末 ID 表示
        // 端末の固有識別番号の取得
        String androidId = Settings.Secure.getString(
                this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        t_id = (TextView) findViewById(R.id.t_id);
        t_id.setText("端末ID：" + androidId);

        // ---------------------- イベント用 ボタン -------------------
        // アカウント 確定ボタン
        // mozi_c_btn = findViewById(R.id.mozi_c_btn);
        account_put_btn = (MaterialButton) findViewById(R.id.account_put_btn);
        setting_01 = (MaterialButton) findViewById(R.id.setting_01);
        kousin_w = (MaterialButton) findViewById(R.id.kousin_w);

        // ------- 判別 フラグ 初期設定
        Ac_Flg = 0;
        db_Flg = 0;

        Item_tb_Flg = 0; // 0: 未作成 1: 作成済み

        // === webview セット
        webView = new WebView(this);

        /*
         * LinearLayout layout = new LinearLayout(this);
         * layout.setOrientati
         * on(LinearLayout.VERTICAL);
         * layout.addView(webView);
         * 
         */

        /**
         * ================== パーミッションリクエスト ====================
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // アクセス許可がすでに与えられている場合の処理
                System.out.println("========== パーミッション 許可済み ===========");

                /**
                 * 削除 function
                 */
                String fileName = "Toyama_Touhoku_Tana_App.apk";
                // パーミッションが許可されている場合はファイルを削除
                deleteFileFromDownloads(this, fileName);

                String fileName_02 = "Toyama_Touhoku_Tana_App (1).apk";
                // パーミッションが許可されている場合はファイルを削除
                deleteFileFromDownloads(this, fileName_02);

                deleteFile();

            } else {
                System.out.println("========== パーミッション 許可を取る～ ===========");
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {

            /**
             * Android 9以下の処理
             */

            System.out.println("========== ビルドバージョン変えて～ ===========");

            requestWriteExternalStoragePermission();
        }

        // ------------------ 担当コード 判別用 SQL
        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        if (doesTableExist(db, "TNMF_table")) {
            // テーブルが存在する場合の処理
            User_Conf();
        } else {
            // テーブルが存在しない場合の処理
        }

        /**
         * 「最新アプリ ダウンロードボタン」
         */
        kousin_w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ----- アプリ ダウンロード ----
                String jim_Apk = "http://192.168.254.226/tana_touhoku_phppost_file/JimApk/index.php"; // **** 社内 *****
                String Toyama_Touhoku_Apk = "http://192.168.3.5/tana_phppost_file_tym/JimApk/index.php"; // 外山産業 東北

                setContentView(webView);
                // jim テスト URL:
                webView.loadUrl(Toyama_Touhoku_Apk); // 本番用

            }
        });

        // ----------- アカウント 作成ボタン フォーカスを受けとった後の 処理 ------------------
        user_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // TODO 自動生成されたメソッド・スタブ
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                // ## フォーカスを受け取ったとき
                if (hasFocus) {
                    // ソフトキーボードの表示
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    user_input.setRawInputType(Configuration.KEYBOARD_QWERTY); // ソフトキーボード の デフォルト設定

                } else {
                    // ## フォーカスが外れた時
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
        });
        // ----------- アカウント 作成ボタン フォーカスを受けとった後の 処理 END ------------------>

        // ----------- 設定一覧 画面へ 移動 ボタン --------------------------------
        setting_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), com.example.Toyama_Touhoku_Tana.Configuration.class);
                startActivity(intent);

                finish();

            }
        }); // ----------- DB 作成ボタン END -------------------------------->

        // **************** 担当コード 入力 テキストエディット キーボード エンター処理が押された処理 *************
        // ************************************************************************************//
        user_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                // エンターボタンが押されたら
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    if (event.getAction() == KeyEvent.ACTION_UP) {

                        // ソフトキーボードを隠す
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(v.getWindowToken(), 0);

                        // エディットテキスト 値 取得
                        g_account = user_input.getText().toString();

                        // ********** ログイン用 情報 SELECT
                        User_Conf();

                    }
                    return true;
                }

                return false;
            }
        });

        // ---------- アカウント作成ボタン イベント処理 Start -----------------
        account_put_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // -------- 棚卸し画面へ csv TNMF.csv １つめの番号を渡す

                g_account = user_input.getText().toString();

                // ******************** エラー処理 ************************

                // ------------- ユーザー番号が空だった場合の 処理
                if (g_account.length() == 0) {

                    // ソフトキーボードを隠す
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);

                    Snackbar.make(v, "入力欄が空白です。「担当コード」を入力してください。", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }

                /**
                 *** //----------- 担当者コード 入力欄が 空じゃなかった 時の判定
                 */
                if (g_account.length() != 0) {

                    /**
                     * 「確定」 ボタン 処理 ------------------
                     *
                     ** ユーザーが 入力した文字が、 配列 arr_col に 担当者コードが存在していた場合の処理
                     **
                     */

                    if (arr_col.contains(g_account)) {

                        // ソフトキーボードを隠す
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(v.getWindowToken(), 0);

                        // ************ SQL アカウント SELECT Start ************* //

                        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());

                        SQLiteDatabase Ac_db = helper.getReadableDatabase();

                        String[] arr_item = new String[2];

                        try {

                            // -------------- トランザクション 開始 -------------------//
                            Ac_db.beginTransaction();

                            // g_account = エディットテキスト 上段 データ
                            Cursor cursor = Ac_db.rawQuery("SELECT TNMF_c_01, TNMF_c_02 " +
                                    "FROM TNMF_table WHERE TNMF_c_01 = " + "\"" + g_account + "\"" + " LIMIT 1", null);

                            if (cursor.moveToNext()) {

                                int idx = cursor.getColumnIndex("TNMF_c_01"); // ログインID
                                arr_item[0] = cursor.getString(idx);

                                idx = cursor.getColumnIndex("TNMF_c_02"); // 担当者名
                                arr_item[1] = cursor.getString(idx);

                                Account_Id = arr_item[0]; // ログイン ID 取得
                                Account_Name = arr_item[1]; // ログイン 名取得

                                System.out.println("Account_Id:::Ac_db:::" + Account_Id);
                                System.out.println("Account_Name:::Ac_db:::" + Account_Name);

                                // --------------------- トランザクション コミット --------------------//
                                Ac_db.setTransactionSuccessful();

                            }

                        } finally {
                            // ------------------------ トランザクション 終了 -----------------------//
                            Ac_db.endTransaction();
                            Ac_db.close();

                        }

                        Get_ID = t_id.getText().toString();
                        // putExtra で データを渡す ---------------------------------
                        Intent intent = new Intent(getApplication(), TopMenu.class);

                        /**
                         * 東北 アカウントID , アカウント名 取得
                         */
                        intent.putExtra("Account_Id", Account_Id); // アカウントID
                        intent.putExtra("Account_Name", Account_Name); // アカウント 名

                        intent.putExtra("Get_ID", Get_ID); // 端末 ID 取得

                        startActivity(intent);

                        // ************* ログイン OK
                        finish();

                    } else {

                        // ソフトキーボードを隠す
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(v.getWindowToken(), 0);

                        // toastMake("入力された「担当者コード」は存在しません。確認してください。",0,-200);

                        // エラー用 スナックバー

                        Snackbar.make(v, "「ログイン エラー」 " + "\n" + "入力された「担当コード」は存在しません。", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // アクセス許可が与えられた場合の処理

                    String fileName = "Toyama_Touhoku_Tana_App.apk";
                    Uri uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(fileName)
                            .build();

                    int rowsDeleted = getContentResolver().delete(uri, null, null);
                    if (rowsDeleted > 0) {
                        // ファイルの削除に成功した場合の処理
                        System.out.println("===================== 削除成功 =====================");
                    } else {
                        // ファイルの削除に失敗した場合の処理
                        System.out.println("===================== 削除 ダメ～ =====================");
                    }

                } else {
                    // アクセス許可が拒否された場合の処理
                    System.out.println("===================== アクセス許可なし～ =====================");
                    requestWriteExternalStoragePermission();
                }
            }
        }
    }

    // ---------- アカウント作成ボタン イベント処理 END ----------------->

    private void toastMake(String message, int x, int y) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);

        // 位置調整
        toast.setGravity(Gravity.CENTER, x, y);
        toast.show();
    }

    /**
     * SQL から ユーザー 一覧を取得する
     */
    private void User_Conf() {

        helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] arr_item = new String[2];

        try {

            Cursor cursor = db.rawQuery("SELECT TNMF_c_01,TNMF_c_02  FROM TNMF_table;", null);

            if (cursor.moveToFirst()) {

                do {

                    int idx = cursor.getColumnIndex("TNMF_c_01"); // ログインID
                    arr_item[0] = cursor.getString(idx);

                    idx = cursor.getColumnIndex("TNMF_c_02"); // 担当者名
                    arr_item[1] = cursor.getString(idx);

                    arr_col.add(arr_item[0]); // === ログイン ID 判定用

                    TNMF_item_User.put(arr_item[0], arr_item[1]); // ログインID : 担当者 ハッシュマップ

                } while (cursor.moveToNext()); // ------ END while

            } // ------ END if

            // --- 出力テスト ---
            for (String a : arr_col) {
                System.out.println(a);
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            if (db != null) {
                db.close();
            }

        }

    }

    /**
     * Android 9以下 のパーミッション確認方法
     *
     */
    // パーミッションのリクエスト結果を受け取るコールバック
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが許可された場合はファイルの削除処理を実行
                deleteFileFromDownloadDirectory();

            } else {
                // パーミッションが拒否された場合はユーザーに通知などの処理を行う
                // ...
            }
        }
    }

    private void init() {

        user_input.setText("");
        user_view.setText("");

    }

    /**
     * テーブル存在 確認
     */

    private boolean doesTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name=?",
                new String[] { tableName });
        boolean tableExists = (cursor != null) && (cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return tableExists;
    }

    /***
     * WebVIew 戻るボタン 処理
     */
    @Override
    public void onBackPressed() {
        // 戻るボタンが押されたときの処理を記述
        if (webView.canGoBack()) {
            webView.goBack(); // WebViewが前のページに戻る
        } else {
            super.onBackPressed(); // 通常の戻るボタンの挙動を実行
        }
    }

    /**
     * 内部ストレージ ダウンロードフォルダ 内 ファイル削除
     */
    private void deleteFile() {

        // === 使用
        String downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();
        File downloadFolder = new File(downloadFolderPath);
        File[] files = downloadFolder.listFiles();

        for (File file : files) {
            String fileName = file.getName(); // ファイル名取得
            String filepaht = file.getPath(); // パス取得

            System.out.println("fileName:::" + fileName);
            System.out.println("filepaht:::" + filepaht);
        }

        // 内部ストレージパス
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "Toyama_Touhoku_Tana_App.apk";

        String downloadDir_tmp = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator + "Toyama_Touhoku_Tana_App.apk";
        System.out.println("downloadDir_tmp:::" + downloadDir_tmp);

        String downloadDir_001 = File.separator + "ome_2021-1.jpeg";
        System.out.println("downloadDir_001:::" + downloadDir_001);

        // 外部ストレージパス
        // String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) +
        // File.separator + "Toyama_Touhoku_Tana_App.apk";
        System.out.println("filePath:::" + filePath);

        String fileName = "";

        /**
         * === 削除処理 === Files.delete(path);
         */
        try {

            File[] filess = downloadFolder.listFiles();

            for (File file : filess) {
                fileName = file.getName(); // ファイル名取得
                System.out.println("fileName for:::" + fileName);

                if (fileName.equals("tanaoroshi.apk")) {

                    System.out.println(" =========== Files.delete(path) 削除処理　開始 ===========");

                    Path path = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        path = Paths.get(file.getPath());
                    }
                    System.out.println("file.getPath():::" + file.getPath());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.delete(path);
                    }
                    System.out.println(" Files.delete(path) ::: ファイルが削除されました。");

                } else if (fileName.equals("Toyama_Touhoku_Tana_App (1).apk")) {

                    System.out.println(" =========== Files.delete(path) 削除処理　開始 ===========");

                    Path path = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        path = Paths.get(file.getPath());
                    }
                    System.out.println("file.getPath():::" + file.getPath());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.delete(path);
                    }
                    System.out.println(" Files.delete(path) ::: ファイルが削除されました。");

                }

                String filepaht = file.getPath(); // パス取得

                System.out.println("fileName:::" + fileName);
                System.out.println("filepaht:::" + filepaht);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ファイルの削除中にエラーが発生しました。");
        }

    }

    /**
     * ファイル削除 media api
     */
    private void deleteFileFromDownloads(Context context, String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri downloadsUri = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }

        System.out.println("========== downloadsUri:::===========" + downloadsUri);

        int deletedRows = 0;
        try {

            String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[] { fileName };

            deletedRows = contentResolver.delete(downloadsUri, selection, selectionArgs);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (deletedRows > 0) {
            Log.d(TAG, "============== 削除 OK ===============");
        } else {
            Log.d(TAG, "============== 削除 NG もうちょいよ～ ===============");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // バックグラウンドからの復帰時の処理

        /**
         * APK 削除処理
         */
        String fileName = "Toyama_Touhoku_Tana_App.apk";
        // パーミッションが許可されている場合はファイルを削除
        deleteFileFromDownloads(this, fileName);

        String fileName_02 = "Toyama_Touhoku_Tana_App (1).apk";
        // パーミッションが許可されている場合はファイルを削除
        deleteFileFromDownloads(this, fileName_02);

        // ＝＝＝ deleteFile 関数実行
        deleteFile();

        /**
         * APK android 9以下 削除処理
         */
        requestWriteExternalStoragePermission();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // バックグラウンドからの復帰時の処理
        user_input.setText("");
        user_view.setText("");

        /**
         * APK 削除処理
         */
        String fileName = "Toyama_Touhoku_Tana_App.apk";
        // パーミッションが許可されている場合はファイルを削除
        deleteFileFromDownloads(this, fileName);

        String fileName_02 = "Toyama_Touhoku_Tana_App (1).apk";
        // パーミッションが許可されている場合はファイルを削除
        deleteFileFromDownloads(this, fileName_02);

        // ＝＝＝ deleteFile 関数実行
        deleteFile();

        /**
         * APK android 9以下 削除処理
         */
        requestWriteExternalStoragePermission();
    }

    // パーミッションのリクエスト
    private void requestWriteExternalStoragePermission() {
        // パーミッションが既に許可されているかチェック
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // パーミッションをリクエスト
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // パーミッションがすでに許可されている場合はファイルの削除処理を実行
            deleteFileFromDownloadDirectory();
        }
    }

    // ダウンロードディレクトリ内のファイルを削除する処理
    private void deleteFileFromDownloadDirectory() {

        String downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();
        File downloadFolder = new File(downloadFolderPath);

        /**
         * === 削除処理 === Files.delete(path);
         */

        String fileName = "";

        File[] files = downloadFolder.listFiles();

        for (File file : files) {
            fileName = file.getName(); // ファイル名取得
            System.out.println("fileName for:::" + fileName);

            if (fileName.equals("Toyama_Touhoku_Tana_App.apk")) {

                System.out.println(" =========== Files.delete(path) 削除処理　開始 ===========");

                // === ファイル削除
                file.delete();
                System.out.println(" Android 9 以下 ファイル削除 成功 ::: ファイルが削除されました。");

            } else if (fileName.equals("Toyama_Touhoku_Tana_App (1).apk")) {

                System.out.println(" =========== Files.delete(path) 削除処理　開始 ===========");
                // === ファイル削除
                file.delete();
                System.out.println("Android 9 以下 ファイル削除 成功 ::: ファイルが削除されました。");

            }

            String filepaht = file.getPath(); // パス取得

            System.out.println("Android 9 以下 fileName:::" + fileName);
            System.out.println("Android 9 以下 filepaht:::" + filepaht);
        }

    } // === deleteFileFromDownloadDirectory END

}
