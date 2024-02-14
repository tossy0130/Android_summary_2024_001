package com.example.Toyama_Touhoku_Tana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReadBerCode extends AppCompatActivity {

    //---------- 画面　全体 ----------
    private ScrollView mainLayout_ber;

    private IntentIntegrator integrator;

    //---------- インテント用　リクエストコード -------
    private final int REQUESTED_LIST = 11; // リスト一覧　へ　遷移
    private final int REQUESTED_QR = 100; // カメラ　起動画面へ遷移
    private final int GET_LIST = 111; // リスト一覧　から　戻ってくる

    private Snackbar snackbar;

    //---------- リストアダプター
   private SelectSheetListView.MyBaseAdapter myBaseAdapter;

    //--------- DB 関連 -----------
    private TestOpenHelper helper;
    private SQLiteDatabase db;

    private DBAdapter DBHelper;

    private ContentValues values; // Send_db インサート 用

    //id
    private long id;

    //---------- header ----------//
    private ImageButton back_btn_02;
    private TextView user_num_02;

    // ユーザーアカウント
    private String gg_accont; // id
    private String gg_accont_02; // name

    //----------- スピナー
    private Spinner top_category_spi;
    private Spinner hinmoku_spi;

    private String spi_item;

    //------------ スピナー　判別用
    // 品区
    private Map<String, String> spi_map = new HashMap<>();

    // 倉庫
    private Map<String ,String> Souko_Hash_Map = new HashMap<>();
    private int max_num;

    private String br_val_01_num;

    //---------- header END ----------//

    //---------- スナックバー　設定 ----//
    private Snackbar snackbar_01;
    private Snackbar snackbar_02;

    //---------- コンテンツ　部分 -------//

    // ボタン　------------------
    private Button reade_br_btn;
    // 表示用　テキストエディット
    private TextView h_moku_m_label;
    private TextView h_moku_m_text; // 品目コード 項目5:  SHKB.csv と、 SHMF.csv で照合
    private TextView h_moku_b_text; // 品目名称　 項目:7  例:プラスチック収納庫　１７３
    private TextView h_moku_text; // 品目備考  項目:8
    private TextView h_moku_text_area; // 品目コード　テキストエリア部分　項目:5 => 8 品目備考と一緒  例：　009850GUBK
    private TextView locashin_label;
    private TextView location_text;

    // --------- DB 保存用 区分 ------------
    private String h_value_01; // Jan コード
    private String h_value_02; //  品目コード
    private String h_value_03; //  品目名称
    private String h_value_04; //  品目備考
    private String h_value_05;
    private String h_value_06;
    private String h_value_07;
    private String h_value_08;

    private String br_val_01; // 品目区分


    // エディットテキスト QR 手打ち　用　取得
    private String edit_qr;

    private TextView max_data_view;

    // バーコード　取得用 テキストビュー
    private TextView br_text;
    // 現品票コード用　（バーコード）
    private EditText qr_edit;

    //　バーコード　取得用 DB 変数 ------------------
    private String SH_col_2; // カラム 02
    private String SH_col_3; // カラム 03
    private String SH_col_6; // カラム 06
    private String SH_col_7; // カラム 07
    private String SH_col_8; // カラム 08
    private String SH_col_9; // カラム 09

    public String test_t;

    private String spi_str;

    private String pre_text;
    private TextView location_name,tana_sum_text;

    //---------- コンテンツ　部分 END -------//

    //---------- 棚卸し　ボトム コンテンツ　部分 Start -------//

    // ボタン
    private Button sql_load;
    private Button sql_read;

    // エディットテキスト---------
    private EditText tana_edit;
    private String tmp_tanasuu;

    //---------- 棚卸し　ボトム コンテンツ　部分 END -------//

    //---------- 読み込み　書き込み　部分 start ----------
    private int item_num;
    private int count_num = 1;

    //---------- 読み込み　書き込み　部分 END ------------>

    //---------- アカウントデータ　送受信用 ----------------

    static final int RESULT_SUBACTIVITY = 1000;

    //---------- カレンダー用 テキストビュー ------------------
    private TextView tokei_view;

    //--------- エディットテキスト　フォーカス　外しよう VIew ------
    private TextView focusView_01;

    //--------- バーコード　読み取り　起動用　フローティングアクションボタン ----
    private FloatingActionButton fab_btn;

    /**
     * 　東北支店　 倉庫　、　ロケーションコード　 , 他
     */
    private String tmp, Scouko_Name, Scan_Str, Get_Souko_name, Get_Loca_Edit;
    private TextView souko_name,jancode_text,irisuu_text,case_sum_text, location_check_setumei;
    private EditText case_num_edit,bara_num_edit;
    private String Account_Id,Account_Name;
    private int JAN_count, JAN_count_02;

    private ArrayList<String> JAN_Shoushi_C_List = new ArrayList<>(); // JAN 重複用 : 商品コード
    private ArrayList<String> JAN_Shoushi_Name_List = new ArrayList<>(); // JAN 重複用 : 商品名
    private String Shouhin_C_01, Shouhin_C_02, Shouhin_Name_01, Shouhin_Name_02, msg_bilder, Souko_Code_Insert;
    private String Shouhin_C_03, Shouhin_Name_03,GET_sql_Jan_Code, Get_Dialog_Loca,Get_ID;
    private AlertDialog dialog;
    private EditText dia_chack_01_01_edit;
    private MaterialButton dia_touroku_btn_001, dia_touroku_btn_002;
    private String Shouhin_Code_Insert, Shouhin_Name_Insert, Location_Code_Insert, Iri_Num_Insert, Tana_Num_Insert;
    private String Now_date_str, get_year, get_month,get_day, Get_Souko_Code, Now_date_str_Insert;
    private String get_hour,get_minute,get_second;

    private static final int REQUEST_CODE_SCAN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_ber_code);

        // ----------- トーストで　イベントリスナーの通知
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // 初期設定 findViewById
        findViews();

        //------- エディットテキスト　インプットタイプ設定

        qr_edit.setInputType(InputType.TYPE_CLASS_NUMBER); // 「JAN、現品票、」　数字　入力　
        case_num_edit.setInputType(InputType.TYPE_CLASS_NUMBER); // ケース数 入力

        //---------- ユーザー ID と、　課　の紐づけ ---------//
      //  getAcount();

        //　場所　情報　取得
       // getPr();

        // 品目コードテーブル　値取得
        // Spiiner_03();

        //-------------------- アカウント情報を受け取る-------------
       if(getIntent() != null) {
           gg_accont = getIntent().getStringExtra("bb_id");
           gg_accont_02 = getIntent().getStringExtra("bb_name");

           /**
            *  Warehouse_Code から　値取得　倉庫名 , ロケーション名
            */
           // === QR コード読取りからの値
           Scouko_Name = getIntent().getStringExtra("Scouko_Name"); // 倉庫名
           Scan_Str = getIntent().getStringExtra("Scan_Str"); // ロケーション名

           // === エディットテキストからの値
           Get_Souko_name = getIntent().getStringExtra("Get_Souko_name"); // 倉庫名
           Get_Loca_Edit = getIntent().getStringExtra("Get_Loca_Edit"); // ロケーション名

           Account_Id = getIntent().getStringExtra("Account_Id"); // アカウントID
           Account_Name = getIntent().getStringExtra("Account_Name"); // アカウント 名

           Get_ID = getIntent().getStringExtra("Get_ID"); // 端末 ID 取得

           user_num_02.setText(Account_Name); // アカウント名　セット

           System.out.println("出力テスト Account_Id:::" + Account_Id);
           System.out.println("出力テスト Get_ID:::" + Get_ID);

           // === 倉庫名セット QR 、エディットテキスト用ロジック
           if(Scouko_Name != null) {
               souko_name.setText(Scouko_Name); // 倉庫名 （QR）
               location_name.setText(Scan_Str); // ロケーション （QR）
           } else {
               souko_name.setText(Get_Souko_name); // 倉庫名 （エディットテキスト）
               location_name.setText(Get_Loca_Edit); // ロケーション （エディットテキスト）
           }


       }




        fab_btn = (FloatingActionButton) findViewById(R.id.fab_btn);

        //---------- back btn バックボタン　を押されたら　値を返す
        back_btn_02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // アカウント　ID , name を返す
                Intent intent = new Intent();
                if(user_num_02.getText() != null) {
                    String str = user_num_02.getText().toString();

                    // intentへ添え字付で値を保持させる
                    intent.putExtra("re_id",str);

                    /**
                     *  東北　
                     */
                    intent.putExtra("Get_Souko_name",Get_Souko_name); // 倉庫名
                    intent.putExtra("Get_Loca_Edit", Get_Loca_Edit); // ロケーション名

                    intent.putExtra("Account_Id",Account_Id); // アカウント ID
                    intent.putExtra("Account_Name", Account_Name); // アカウント 名

                    intent.putExtra("Get_ID", Get_ID); // 端末ID 取得

                    // 返却したい結果ステータスをセットする
                    setResult( Activity.RESULT_OK, intent);

                    // アクティビティを終了させる
                    finish();
                }

            }
        });

        /**
         *  fab_btn が　タップされた時の処理 ---------------------------------
         */
        //--------------------- QR 読み取り　-------------------------//
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 integrator = new IntentIntegrator(ReadBerCode.this);
                // キャプチャ画面の下方にメッセージを表示
                integrator.setPrompt("戻る　ボタン タップで「キャンセル」できます。");
                // キャプチャ画面起動

                integrator.initiateScan();
                
            }
        });
        //--------------------- QR 読み取り END　-------------------------//


        /**
         *  　＝＝＝＝＝＝＝＝　ケース数　＝＝＝＝＝＝＝　
         */
        //------------ ケース数　イベント --------------------
        case_num_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // TODO 自動生成されたメソッド・スタブ
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // ## フォーカスを受け取ったとき
                if(hasFocus) {
                    // ソフトキーボードの表示
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    case_num_edit.setRawInputType(Configuration.KEYBOARD_QWERTY); // ソフトキーボード　の　デフォルト設定

                } else {
                    // ## フォーカスが外れた時
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);

                    /**
                     *  フォーカスが外れて、ケース数が入力されていた場合は
                     */
                    String Tmp_case_num_edit = case_num_edit.getText().toString(); // ケース数
                    // 空の場合
                    if(Tmp_case_num_edit.isEmpty() || Tmp_case_num_edit.equals("")) {

                        String bara_num_edit_01 = bara_num_edit.getText().toString(); // バラ数

                        // === バラ数が 「空」の場合
                        if(bara_num_edit_01.isEmpty() || bara_num_edit_01.equals("")) {
                            return;
                        } else {
                            // バラ数が 「空」　じゃない場合
                            tana_sum_text.setText(bara_num_edit_01);
                        }

                        return;
                    } else {
                        // === ケース数をセット

                        String Tmp_bara_num_edit = bara_num_edit.getText().toString(); // バラ数

                        // === バラ数が　「空」　だったら
                        if(Tmp_bara_num_edit.isEmpty() || Tmp_bara_num_edit.equals("")) {

                            try {

                                String Tmp_irisuu_text = irisuu_text.getText().toString(); // 入数
                                int Tmp_irisuu_text_i = Integer.parseInt(Tmp_irisuu_text); // 入数 int
                                int Tmp_case_num_edit_i = Integer.parseInt(Tmp_case_num_edit); // ケース数 int
                                int Tmp_Case_Iri_Sum = Tmp_irisuu_text_i * Tmp_case_num_edit_i; // ケース数 * 入数

                                case_sum_text.setText(String.valueOf(Tmp_Case_Iri_Sum)); // ケース数合計へセット

                                tana_sum_text.setText(String.valueOf(Tmp_Case_Iri_Sum)); // 棚卸し数合計へセット

                            } catch (NumberFormatException e){
                                e.printStackTrace();
                            }

                        } else {

                            // === バラ数が 「空」　じゃなかったら
                            try {

                                String Tmp_irisuu_text = irisuu_text.getText().toString(); // 入数
                                int Tmp_irisuu_text_i = Integer.parseInt(Tmp_irisuu_text); // 入数 int
                                int Tmp_case_num_edit_i = Integer.parseInt(Tmp_case_num_edit); // ケース数 int
                                int Tmp_Case_Iri_Sum = Tmp_irisuu_text_i * Tmp_case_num_edit_i; // ケース数 * 入数

                                case_sum_text.setText(String.valueOf(Tmp_Case_Iri_Sum)); // ケース数合計へセット

                                int Tmp_bara_num_edit_i = Integer.parseInt(Tmp_bara_num_edit); // バラ数 int

                                int Tana_Sum = Tmp_bara_num_edit_i + Tmp_Case_Iri_Sum; // バラ数 + ケース数

                                tana_sum_text.setText(String.valueOf(Tana_Sum)); // 棚卸し合計へセット

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                        }


                    }  // === END IF

                }

            }
        });

        //---------------- ケース数　エディットテキストで　ボタンを押された時の処理 ---------------------
        case_num_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                // エンターボタンが押されたら
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT){

                    // ソフトキーボード 非表示
                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    String Case_Num = case_num_edit.getText().toString();
                    System.out.println("Case_Num:::" + Case_Num);
                    // === ケース数が「空」か「０」じゃない場合
                    if(Case_Num.isEmpty() || Case_Num.equals("0")) {

                        System.out.println("Case_Num Point 01:::");
                       // toastMake("ケース数を入力してください。", 0, -200);
                        return false;

                    } else {

                        System.out.println("Case_Num Point 02:::");
                        // === バラ数の値取得
                        String Get_Bara_Num = bara_num_edit.getText().toString();

                        // ====== バラ数が空だった場合　の処理
                        if(Get_Bara_Num.isEmpty()) {

                            try {

                                System.out.println("Case_Num Point 03:::");
                                // === 入り数取得
                                String Iri_num = irisuu_text.getText().toString();
                                if(!(Iri_num.equals("0"))) {
                                    int Iri_num_i = Integer.parseInt(Iri_num);
                                    int Case_num_i = Integer.parseInt(Case_Num);

                                    int Case_SUM = Iri_num_i * Case_num_i;
                                    case_sum_text.setText(String.valueOf(Case_SUM));

                                    // === フォーカス移動　バラ数
                                    bara_num_edit.requestFocus(); // EditTextにフォーカスを移動
                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                                } else {
                                    toastMake("入り数が「０」です。", 0, -200);
                                    case_num_edit.setText("0");
                                    case_sum_text.setText("0");

                                    // === フォーカス移動　バラ数
                                    bara_num_edit.requestFocus(); // EditTextにフォーカスを移動
                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                                    return false;
                                }

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                        } else {

                            System.out.println("Case_Num Point 04:::");
                            // ====== バラ数が空じゃなかったら
                            try {

                                // === ケース数を求める　
                                String Iri_num = irisuu_text.getText().toString();
                                int Iri_num_i = Integer.parseInt(Iri_num);
                                int Case_num_i = Integer.parseInt(Case_Num);
                                int Case_SUM = Iri_num_i * Case_num_i;
                                case_sum_text.setText(String.valueOf(Case_SUM));

                                // === バラ数　と　ケース数の合計を求める
                                String Get_Case_Num = case_sum_text.getText().toString();
                                int Case_Num_i = Integer.parseInt(Get_Case_Num); // ケース数
                                int Bara_Num_i = Integer.parseInt(Get_Bara_Num); // バラ数

                                int Tana_SUM_i = Case_Num_i + Bara_Num_i;

                                // === 棚卸し数　合計
                                tana_sum_text.setText(String.valueOf(Tana_SUM_i));

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                }

                return false;
            }
        });

        /**
         *  　＝＝＝＝＝＝＝＝　バラ数　＝＝＝＝＝＝＝　
         */
        //------------ バラ数　イベント --------------------
        bara_num_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // TODO 自動生成されたメソッド・スタブ
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // ## フォーカスを受け取ったとき
                if(hasFocus) {
                    // ソフトキーボードの表示
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    bara_num_edit.setRawInputType(Configuration.KEYBOARD_QWERTY); // ソフトキーボード　の　デフォルト設定

                    // === バラ数取得
                    String Get_Bara_Num = bara_num_edit.getText().toString();
                    if(Get_Bara_Num.isEmpty()) {

                    } else {
                        //**************** エディットテキスト　フォーカス　移動　※ケース数
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        //****************

                    }

                } else {


                    String Tmp_case_num_edit = case_num_edit.getText().toString(); // === ケース数取得
                    // ====== ケース数が 空 の場合
                    if(Tmp_case_num_edit.isEmpty() || Tmp_case_num_edit.equals("")) {

                        String Tmp_bara_num_edit = bara_num_edit.getText().toString(); // バラ数取得
                        // バラ数が空の場合
                        if(Tmp_bara_num_edit.equals("") || Tmp_bara_num_edit.isEmpty()) {
                            return;
                        } else {
                            // === ケース数が「空」で、バラ数が入力されていたら
                            tana_sum_text.setText(Tmp_bara_num_edit);
                        }

                    } else {


                            // ====== ケース数が 空 じゃない場合
                            String Tmp_bara_num_edit_02 = bara_num_edit.getText().toString(); // バラ数取得
                            // === バラ数が 「空」 の場合
                            if(Tmp_bara_num_edit_02.equals("") || Tmp_bara_num_edit_02.isEmpty()) {

                                try {

                                    int Tmp_case_num_edit_i = Integer.parseInt(Tmp_case_num_edit); // ケース数 int
                                    String Tmp_irisuu_text =  irisuu_text.getText().toString(); // 入数
                                    int Tmp_irisuu_text_i = Integer.parseInt(Tmp_irisuu_text); // 入数 int

                                    case_sum_text.setText(String.valueOf(Tmp_irisuu_text_i)); // バラ数　計　へセット

                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

                        } else {

                                try {

                                    int Tmp_case_num_edit_i = Integer.parseInt(Tmp_case_num_edit); // ケース数 int
                                    String Tmp_irisuu_text =  irisuu_text.getText().toString(); // 入数
                                    int Tmp_irisuu_text_i = Integer.parseInt(Tmp_irisuu_text); // 入数 int

                                    int Case_Sum = Tmp_case_num_edit_i * Tmp_irisuu_text_i; // ケース数 * 入数
                                    case_sum_text.setText(String.valueOf(Case_Sum)); // ケース数　計　へセット



                                    int Tmp_bara_num_edit_02_i = Integer.parseInt(Tmp_bara_num_edit_02); // バラ数 int
                                    int Tana_Sum = Case_Sum + Tmp_bara_num_edit_02_i; // ケース数　+ バラ数

                                    tana_sum_text.setText(String.valueOf(Tana_Sum)); // 棚卸し　合計数　セット

                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

                        }

                    }

                    // ## フォーカスが外れた時
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);

                }

            }
        });

        //---------------- バラ数　エディットテキストで　ボタンを押された時の処理 ---------------------
        bara_num_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                // エンターボタンが押されたら
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                    // ソフトキーボード 非表示
                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    String Bara_Num = bara_num_edit.getText().toString();
                    System.out.println("Bara_Num:::" + Bara_Num);
                    // === ケース数が「空」か「０」じゃない場合
                    if(Bara_Num.isEmpty()) {
                        bara_num_edit.setText("0");

                        String Get_Case_Num = case_sum_text.getText().toString();
                        String Get_Bara_Tmp_Num = bara_num_edit.getText().toString();
                        if(!Get_Case_Num.isEmpty()) {
                            int Case_Num_i = Integer.parseInt(Get_Case_Num); // ケース数
                            int Bara_Num_i = Integer.parseInt(Get_Bara_Tmp_Num); // バラ数

                            int Tana_SUM_i = Case_Num_i + Bara_Num_i;

                            // === 棚卸し数　合計
                            tana_sum_text.setText(String.valueOf(Tana_SUM_i));
                        } else {

                            //**************** エディットテキスト　フォーカス　移動　※ケース数
                            case_num_edit.setFocusableInTouchMode(true);
                            case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                            //****************
                        }

                        return false;

                    } else {
                        // === ケース数　取得

                        try {

                            String Get_Case_Num = case_sum_text.getText().toString();

                            // === ケース数が空だったら 0 をセット
                            if(Get_Case_Num.isEmpty()) {

                                Get_Case_Num = "0";
                                int Case_Num_i = Integer.parseInt(Get_Case_Num); // ケース数
                                int Bara_Num_i = Integer.parseInt(Bara_Num); // バラ数

                                int Tana_SUM_i = Case_Num_i + Bara_Num_i;

                                case_num_edit.setText(Get_Case_Num); // ケース数に０をセット
                                case_sum_text.setText(Get_Case_Num); // ケース数合計に０をセット
                                // === 棚卸し数　合計
                                tana_sum_text.setText(String.valueOf(Tana_SUM_i));

                            } else {

                                // === ケース数が入力されていたら
                                int Case_Num_i = Integer.parseInt(Get_Case_Num); // ケース数
                                int Bara_Num_i = Integer.parseInt(Bara_Num); // バラ数

                                int Tana_SUM_i = Case_Num_i + Bara_Num_i;


                                // === 棚卸し数　合計
                                tana_sum_text.setText(String.valueOf(Tana_SUM_i));
                            }


                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    }

                }
                return false;
            }

        });

        //------------ QR コード、　現品票コード 、　バーコード　エディットテキスト　イベント --------------------
        qr_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // TODO 自動生成されたメソッド・スタブ
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // ## フォーカスを受け取ったとき
                if(hasFocus) {
                    // ソフトキーボードの表示
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    qr_edit.setRawInputType(Configuration.KEYBOARD_QWERTY); // ソフトキーボード　の　デフォルト設定

                } else {
                    // ## フォーカスが外れた時
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
                }

            }
        });

        /**
         *  ====================================== 手入力検索 =========================================
         */
        //----------------  QR 、　現品票コード、　バーコード　エディットテキストで　ボタンを押された時の処理 ---------------------
        qr_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                // エンターボタンが押されたら
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT){

                    // ソフトキーボード 非表示
                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    }

                    // エディットテキスト　値　取得
                    edit_qr = qr_edit.getText().toString();

                    if(edit_qr.length() != 0 && edit_qr.length() != 13) {

                        /**
                         * // ************ SQL 手打ち バーコード、QR、現品票コード　SELECT  Start ************* //
                         *   function :  SELECT_Shouhin_Code
                         */
                        SELECT_Shouhin_Code(edit_qr);

                        String Tmp_h_moku_text = h_moku_text.getText().toString(); // 商品コード
                        String Tmp_h_moku_m_text = h_moku_m_text.getText().toString(); // 商品名

                            //----------- 検索　キーワード　判定 -------->
                            if(edit_qr == null || edit_qr.equals("") || Tmp_h_moku_text.isEmpty() ||
                                    Tmp_h_moku_text.equals("") || Tmp_h_moku_m_text.isEmpty() || Tmp_h_moku_m_text.equals("")) {

                                Toast.makeText(getApplicationContext(), "「該当」する商品はありません。もう一度入力してください。", Toast.LENGTH_LONG).show();

                                return false;

                            } else {

                                //************** 検索ヒット時の処理 *************//

                            }

                        //**************** エディットテキスト　フォーカス　移動　※ケース数

                        bara_num_edit.setFocusableInTouchMode(true);
                        bara_num_edit.requestFocus();
                        /*
                        case_num_edit.setFocusableInTouchMode(true);
                        case_num_edit.requestFocus();
                         */
                         // EditTextにフォーカスを移動

                        //****************

                        /**
                         *  ================================================================================
                         *  ===============================  バーコード検索 分岐 （手入力 検索）==============================
                         *  ================================================================================
                         */
                    } else if (edit_qr.length() <= 13) {

                        System.out.println(" *********************** バーコード 開始 ***********************");
                        System.out.println("バーコード edit_qr:::" + edit_qr);

                        Case_Bara_Num_Init(); // 棚卸し数関係　初期化（ケース数、バラ数、棚卸し合計数）

                        // ===　倉庫コード　取得
                        String souko_name_tmp = souko_name.getText().toString();
                        String Souko_Code_select = GET_SELECT_Souko_ID(souko_name_tmp);

                        /**
                         *   JANコード 重複チェック
                         */
                        TestOpenHelper j_helper1 = new TestOpenHelper(getApplicationContext());
                        SQLiteDatabase j_db = j_helper1.getReadableDatabase();
                        int T_NUM = 0;
                        int test_count = 0;

                        try {

                            /***
                             *  ＝＝＝＝＝＝＝＝＝ JANコード　検索　（手入力）　＝＝＝＝＝＝＝＝＝
                             */
                            //  Cursor cursor = j_db.rawQuery("SELECT COUNT(*) FROM SHMF_table WHERE SHMF_c_03 = " + "\"" + Scan_Val + "\"" + ";", null);

                            //  SHMF_c_01:商品C, SHMF_c_02:品名, SHMF_c_03:JANコード, SHMF_c_04:品番, SHMF_c_05:入数,RZMF_c_02:倉庫C、RZMF_c_03:棚番,SOMF_c_02:倉庫名、SOMF_c_03 text：棚卸日

                    /*
                    Cursor cursor = j_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03, SHMF_c_04,SHMF_c_05,RZMF_c_02,RZMF_c_03,SOMF_c_02,SOMF_c_03 FROM SHMF_table " +
                            "left outer join RZMF_table on SHMF_table.SHMF_c_01 = RZMF_table.RZMF_c_01 " +
                            "left outer join SOMF_table on SOMF_table.SOMF_c_01 = RZMF_table.RZMF_c_02 " +
                            "WHERE SHMF_table.SHMF_c_03 = " + "\"" + Scan_Val + "\"" + "AND RZMF_c_02 = " + "\"" + Souko_Code_select + "\"" +  ";", null);
                     */


                            Cursor cursor = j_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03 ,SHMF_c_04,SHMF_c_05,t1.RZMF_c_02, t1.RZMF_c_03, SOMF_table.SOMF_c_02, SOMF_table.SOMF_c_03 " +
                                    "FROM SHMF_table " +
                                    "LEFT OUTER JOIN (SELECT * FROM RZMF_table WHERE RZMF_c_02 = " + "\"" + Souko_Code_select + "\"" + ") t1 " +
                                    "ON SHMF_table.SHMF_c_01 = t1.RZMF_c_01 " +
                                    "left outer join SOMF_table on SOMF_table.SOMF_c_01 = t1.RZMF_c_02 " +
                                    "WHERE SHMF_c_03 =  " + "\"" + edit_qr + "\"" , null);

                            // JAN_count_02
                            JAN_count_02 = cursor.getCount();
                            System.out.println("JAN_count_02:::" + JAN_count_02);

                            if (cursor != null && cursor.moveToFirst()) {

                                do {
                                    T_NUM++;
                                    JAN_count = cursor.getCount();
                                    test_count++;
                                    System.out.println("test_count:::" + test_count);
                                } while (cursor.moveToNext());

                            }// ======= END if

                            // === JAN コード検索数
                            System.out.println("cursor.getCount() 01;:::" + cursor.getCount());
                            System.out.println("T_NUM;:::" + T_NUM);
                            System.out.println("JAN_count:::" + JAN_count);

                            /**
                             *  ＝＝＝　取得結果の　カラム数が　１件の場合　＝＝＝
                             */
                            if(JAN_count == 1) {

                                String[] arr_item = new String[7];

                                String edit_qr_num01 = "";
                                String edit_qr_num02 = "";
                                //  String edit_qr_num03 = "";

                                if (cursor != null && cursor.moveToFirst()) {

                                    do {

                                        // カラム 01
                                        int idx = cursor.getColumnIndex("SHMF_c_01"); // 商品C
                                        arr_item[0] = cursor.getString(idx);
                                        edit_qr_num01 = arr_item[0];

                                        Shouhin_Code_Insert = arr_item[0]; // *** 商品コード インサート用

                                        // === テキストビュー へ 商品コードを挿入
                                        h_moku_text.setText(edit_qr_num01);

                                        // カラム 02
                                        idx = cursor.getColumnIndex("SHMF_c_02"); // 品名
                                        arr_item[1] = cursor.getString(idx);
                                        edit_qr_num02 = arr_item[1];

                                        Shouhin_Name_Insert = arr_item[1]; // *** 商品名 インサート用

                                        // INSERT 用　品目コード
                                        SH_col_2 = arr_item[1];
                                        System.out.println(SH_col_2 + "SH_col_2　出力テスト");

                                        // カラム 03
                                        idx = cursor.getColumnIndex("SHMF_c_03"); // JANコード
                                        arr_item[2] = cursor.getString(idx);

                                        // === テキストビュー へ 商品名 挿入
                                        h_moku_m_text.setText(arr_item[1]);

                                        // === jan コード表示用
                                        jancode_text.setText(arr_item[2]);

                                        // カラム 04
                                        idx = cursor.getColumnIndex("RZMF_c_02"); // 倉庫C
                                        arr_item[3] = cursor.getString(idx);

                                        Souko_Code_Insert = arr_item[3]; // *** 倉庫コード インサート用

                                        // カラム 05
                                        idx = cursor.getColumnIndex("SHMF_c_05"); // 入数
                                        arr_item[4] = cursor.getString(idx);

                                        // === 入数が０の場合、１を入れる
                                        if(arr_item[4].equals("0")) {
                                            arr_item[4] = "1";
                                        }

                                        // === 入り数　表示用
                                        irisuu_text.setText(arr_item[4]);

                                        Iri_Num_Insert = arr_item[4]; // *** 入数 インサート用

                                        // カラム 06
                                        idx = cursor.getColumnIndex("RZMF_c_03"); // 棚番（ロケーション）
                                        arr_item[5] = cursor.getString(idx);

                                        /**
                                         *  Fucntion:Location_Num_Check ロケーション　チェック
                                         */

                                        //    Location_Num_Check(arr_item[5]);

                                        // === 商品コード、ロケーションコードをセット
                                        Location_Num_Check_02(arr_item[0], arr_item[5]);

                                        // 商品コード　出力チェック
                                        System.out.println("onActivityResult:::arr_item[0]:::" + arr_item[0]);


                                        String Location_Code = arr_item[5];

                                        // ロケーションコード
                                        //  location_text.setText(Location_Code);

                                        // カラム 07
                                        idx = cursor.getColumnIndex("SOMF_c_02"); // 倉庫名
                                        arr_item[6] = cursor.getString(idx);

                                        // === テキストビューへ　倉庫名をセット
                                        // h_moku_b_text.setText(arr_item[6]);

                                    } while (cursor.moveToNext());

                                }// ======= END if

                                System.out.println("IF １件分岐:::OK =======================");

                                String Tmp_h_moku_text = h_moku_text.getText().toString();
                                // 商品コード　、　倉庫コード　、　商品名が　空だった場合
                                if(Tmp_h_moku_text.isEmpty() && edit_qr_num02.isEmpty()) {
                                    toastMake("該当の商品はありません。",0, -200);
                                    return false;
                                }

                                bara_num_edit.requestFocus();  // バラ数
                                //  case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                // ソフトキーボードを表示する

                                System.out.println(" *********************** バーコード 終了 ***********************");



                            } else if (JAN_count >= 2 || JAN_count != 0) {
                                /**
                                 *  ＝＝＝　取得結果の　カラム数が　2件以上の場合　＝＝＝
                                 */

                                String[] arr_item = new String[7];

                                String edit_qr_num01 = "";
                                String edit_qr_num02 = "";
                                //  String edit_qr_num03 = "";

                                int loop_count = 0;

                                if (cursor != null && cursor.moveToFirst()) {

                                    do {

                                        // カラム 01
                                        int idx = cursor.getColumnIndex("SHMF_c_01"); // 商品C
                                        arr_item[0] = cursor.getString(idx);
                                        edit_qr_num01 = arr_item[0];

                                        // === テキストビュー へ 商品コードを挿入
                                        // h_moku_text.setText(edit_qr_num01);

                                        JAN_Shoushi_C_List.add(arr_item[0]); // 重複している JANコードの商品コード 取得

                                        // カラム 02
                                        idx = cursor.getColumnIndex("SHMF_c_02"); // 品名
                                        arr_item[1] = cursor.getString(idx);
                                        edit_qr_num02 = arr_item[1];

                                        JAN_Shoushi_Name_List.add(arr_item[1]); // 商品名 リスト挿入

                                        // INSERT 用　品目コード
                                        SH_col_2 = arr_item[1];

                                        System.out.println(SH_col_2 + "SH_col_2　出力テスト");

                                        // カラム 03
                                        idx = cursor.getColumnIndex("SHMF_c_03"); // JANコード
                                        arr_item[2] = cursor.getString(idx);

                                        GET_sql_Jan_Code = arr_item[2];

                                        loop_count++;
                                    } while (cursor.moveToNext());

                                }

                                // ====== 配列の要素数を取得して、分岐する
                                int GEt_Zyuufuku_NUM = JAN_Shoushi_C_List.size();
                                System.out.println("GEt_Zyuufuku_NUM:::" + GEt_Zyuufuku_NUM);

                                //--------------- アラートダイアログ　の表示　開始 ---------------------
                                AlertDialog.Builder bilder = new AlertDialog.Builder(ReadBerCode.this);

                                //-------------- カスタムタイトル　作成
                                TextView titleView;
                                titleView = new TextView(ReadBerCode.this);
                                titleView.setText("JANコード"
                                        +  "【" + GET_sql_Jan_Code + "】" + "\n"
                                        + "\n\n" + "重複している商品がありますので、選択してください。");
                                titleView.setTextSize(18);
                                titleView.setTextColor(Color.WHITE);
                                titleView.setBackgroundColor(getResources().getColor(R.color.back_color_01));
                                titleView.setPadding(20, 30, 20, 30);
                                titleView.setGravity(Gravity.CENTER);
                                //-------------- カスタムタイトル　作成 END
                                // ダイアログの項目
                                bilder.setCustomTitle(titleView);

                                /**
                                 *  端末のバックボタンを押した時の処理
                                 */
                                bilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                    @Override
                                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                                            Allaht_Dialog_Clear();

                                            return true; // イベントを処理済みとしてtrueを返す
                                        }
                                        return false; // それ以外のキーイベントは通常の処理に任せるためfalseを返す
                                    }
                                });

                                /**
                                 *  背景以外を押した時の処理
                                 */
                                bilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {

                                        Allaht_Dialog_Clear();

                                        return;

                                    }
                                });

                                switch (GEt_Zyuufuku_NUM) {
                                    // === JAN で重複レコードが２件だった場合
                                    case 2:
                                        Shouhin_C_01 = JAN_Shoushi_C_List.get(0);
                                        Shouhin_C_02 = JAN_Shoushi_C_List.get(1);

                                        Shouhin_Name_01 = JAN_Shoushi_Name_List.get(0);
                                        Shouhin_Name_02 = JAN_Shoushi_Name_List.get(1);

                                        // set dialog message
                                        bilder.setItems(new CharSequence[]{
                                                "【" + Shouhin_C_01 + "】" + ":" + Shouhin_Name_01 + "\n\n",
                                                "【" + Shouhin_C_02 + "】" + ":" + Shouhin_Name_02 + "\n\n",
                                                "キャンセル"
                                        }, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 選択されたアイテムに応じた処理を実行
                                                switch (which) {
                                                    case 0:
                                                        // Item1 が選択された場合の処理
                                                        SELECT_Shouhin_Code(Shouhin_C_01);

                                                        Allaht_Dialog_Clear();

                                                        break;
                                                    case 1:
                                                        SELECT_Shouhin_Code(Shouhin_C_02);

                                                        Allaht_Dialog_Clear();

                                                        break;
                                                    case 2:
                                                        // Item3 が選択された場合の処理

                                                        Allaht_Dialog_Clear();

                                                        break;
                                                }
                                            }
                                        });

                                        dialog = bilder.create();
                                        dialog.show();

                                        break;

                                    // === JAN で重複レコードが、３件だった場合
                                    case 3:
                                        Shouhin_C_01 = JAN_Shoushi_C_List.get(0);
                                        Shouhin_C_02 = JAN_Shoushi_C_List.get(1);
                                        Shouhin_C_03 = JAN_Shoushi_C_List.get(2);

                                        Shouhin_Name_01 = JAN_Shoushi_Name_List.get(0);
                                        Shouhin_Name_02 = JAN_Shoushi_Name_List.get(1);
                                        Shouhin_Name_03 = JAN_Shoushi_Name_List.get(2);

                                        //-------------- ダイアログ　メッセージ内容
                                        msg_bilder = "商品コード 01：" + Shouhin_C_01 + "\n" +
                                                "商品名 01：" + Shouhin_Name_01 + "\n\n" +
                                                "商品コード 02：" +  Shouhin_C_02 + "\n" +
                                                "商品名 02：" + Shouhin_Name_02 + "\n\n" +
                                                "商品名 03：" + Shouhin_Name_03 + "\n" +
                                                "商品コード 03：" +  Shouhin_C_03 + "\n";
                                        bilder.setMessage(msg_bilder);

                                        bilder.setPositiveButton(Shouhin_C_01, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Allaht_Dialog_Clear();

                                                return;
                                            }
                                        });

                                        bilder.setNegativeButton(Shouhin_C_02, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Allaht_Dialog_Clear();

                                                return;
                                            }
                                        });

                                        bilder.setNeutralButton(Shouhin_C_03, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // Button2がクリックされた場合の処理

                                                Allaht_Dialog_Clear();

                                                return;
                                            }
                                        });


                                        dialog = bilder.create();
                                        dialog.show();

                                        break;
                                }

                                System.out.println("IF ２件分岐:::OK =======================");

                            } else {
                                /**
                                 * 　＝＝＝　０件だった場合の処理　＝＝＝
                                 */

                                toastMake("該当の商品はありません。",0, -200);
                                System.out.println("IF ０件分岐:::OK =======================");

                                Save_init(); //　＊＊＊＊＊＊＊＊＊＊＊ テキストビューなどを　初期化　＊＊＊＊＊＊＊＊＊＊＊

                                return false;
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            if(j_db != null) {
                                j_db.close();
                            }
                        }


                    } else {

                        //--------------- エディットテキスト 文字が空だった場合の処理 ----------------->
                        Toast.makeText(getApplicationContext(), "「現品票」 または 「バーコード」を入力してください", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            }
        });

        //----------------　保存　ボタン　処理 -----------------------------
        sql_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // キーボードを非表示
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                //************* 保存ボタン　確定　処理 ****************
                String Result_Num = tana_sum_text.getText().toString(); // 棚卸し数 合計

                String qr_edit_num = qr_edit.getText().toString(); // QR or バーコード　エディット

                String h_moku_text_t = h_moku_text.getText().toString(); // 品目コード テキストボックス

                String h_moku_m_text_t = h_moku_m_text.getText().toString(); // 名称　テキストボックス

                if(Result_Num.equals("")) {
                    toastMake("「棚卸し数」 が空です。", 0,-200);
                    return;

                } else {
                    //----- DB Insert 処理
                    saveList();
                    toastMake("保存完了しました。", 0,-200);
                    System.out.println("DB インサート　完了");



                    System.out.println("DB TestOpenHelper Send_db インサート　完了");
                }
            }
        });

        //-------------- 一覧表示　ボタン　（読み込み）　-------------------
        sql_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 一覧　表示
               // Intent intent = new Intent(getApplication(), List_Display.class);
                Intent intent = new Intent(getApplication(), SelectSheetListView.class);

                intent.putExtra("Account_Id",Account_Id); // アカウント ID
                intent.putExtra("Account_Name", Account_Name); // アカウント 名
                
                startActivity(intent);
            }
        });


        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // タッチイベントをキャンセルして処理を防止
            }
        });

    }  //------------- onCrate END ********************************************************
     // ******************************************************************************
    // -------------------------------------------------------------------------->


    //----------- エディットテキスト　QR、　バーコード、　現品票コード　非表示用　タッチイベント　
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        focusView_01.requestFocus();
        return super.onTouchEvent(event);
    }

    //----------------- メニュー　追加
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qr_menu_01, menu);

        return true;
    }

    //----------------- メニューボタンが押された時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.qr_menu_01_btn:
                integrator = new IntentIntegrator(ReadBerCode.this);
                // キャプチャ画面の下方にメッセージを表示
                integrator.setPrompt("戻る　ボタン タップで「キャンセル」できます。");

                // キャプチャ画面起動
                integrator.initiateScan();
        }

        return true;
    }




    // カメラで　読み取った結果の取得　。　表示
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        String Scan_Val_TMP = scanResult.getContents();
        System.out.println("Scan_Val_TMP:::値::: =======================" + Scan_Val_TMP);



        //このif文で、読み取られたバーコードがJANコードかどうか判定する

        /*
        if (scanResult.getFormatName() != BarcodeFormat.EAN_13){
            System.out.println("Scan_Val_TMP:::値::: =======================" + Scan_Val_TMP);
            return;
        }
         */
        
        if (Scan_Val_TMP == null) {

            System.out.println("if scanResult.getContents() == 戻るボタン =======================");
            Save_init(); //　＊＊＊＊＊＊＊＊＊＊＊ テキストビューなどを　初期化　＊＊＊＊＊＊＊＊＊＊＊
            return;
        }


        String Scan_Val = scanResult.getContents();
        System.out.println("Scan_formatName getFormatName:::値::: =======================" + Scan_Val);

        String Scan_formatName = scanResult.getFormatName();
        System.out.println("Scan_formatName getFormatName:::値::: =======================" + Scan_formatName);

        if (scanResult != null) {

            switch (scanResult.getFormatName()) {
                // ====== 23_1116
                // === JANコード 対応： 0028632931158 （例）
                case "UPC_A": {
                    System.out.println("case UPC_A:::値::: =======================");
                    if (Scan_Val.length() == 12) {
                        Scan_Val = "0" + Scan_Val;
                    }
                    System.out.println("case UPC_A　Scan_Val:::値::: =======================" + Scan_Val);
                    break;
                }

                default:
                    System.out.println("case default :::なにもしない::: =======================");
            }

        }

        /**
         /* * QR データ　取得
         */
        if (scanResult != null) {

            // === 変更 23_1116 夏目
            if (Scan_Val.length() == 7 || Scan_Val.length() != 13) {

                System.out.println("QR------2次元バーコード--------");

                Case_Bara_Num_Init(); // 棚卸し数関係　初期化（ケース数、バラ数、棚卸し合計数）

                /***
                 *  ＝＝＝＝＝＝＝＝＝ 商品コード　検索　＝＝＝＝＝＝＝＝＝
                 */
                SELECT_Shouhin_Code(Scan_Val);

                System.out.println("*********************** ２次元バーコード　で　読み取り OK *********************");

                bara_num_edit.requestFocus(); // ケース数
                //  case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                // ソフトキーボードを表示する

            } else {
                System.out.println(" *********************** バーコード 開始 ***********************");
                System.out.println("バーコード Scan_Val:::" + Scan_Val);

                Case_Bara_Num_Init(); // 棚卸し数関係　初期化（ケース数、バラ数、棚卸し合計数）

                // ===　倉庫コード　取得
                String souko_name_tmp = souko_name.getText().toString();
                String Souko_Code_select = GET_SELECT_Souko_ID(souko_name_tmp);

                /**
                 *   JANコード 重複チェック
                 */
                TestOpenHelper j_helper1 = new TestOpenHelper(getApplicationContext());
                SQLiteDatabase j_db = j_helper1.getReadableDatabase();
                int T_NUM = 0;
                int test_count = 0;

                try {

                    /***
                     *  ＝＝＝＝＝＝＝＝＝ JANコード　検索　＝＝＝＝＝＝＝＝＝
                     */
                    //  Cursor cursor = j_db.rawQuery("SELECT COUNT(*) FROM SHMF_table WHERE SHMF_c_03 = " + "\"" + Scan_Val + "\"" + ";", null);

                    //  SHMF_c_01:商品C, SHMF_c_02:品名, SHMF_c_03:JANコード, SHMF_c_04:品番, SHMF_c_05:入数,RZMF_c_02:倉庫C、RZMF_c_03:棚番,SOMF_c_02:倉庫名、SOMF_c_03 text：棚卸日

                    /*
                    Cursor cursor = j_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03, SHMF_c_04,SHMF_c_05,RZMF_c_02,RZMF_c_03,SOMF_c_02,SOMF_c_03 FROM SHMF_table " +
                            "left outer join RZMF_table on SHMF_table.SHMF_c_01 = RZMF_table.RZMF_c_01 " +
                            "left outer join SOMF_table on SOMF_table.SOMF_c_01 = RZMF_table.RZMF_c_02 " +
                            "WHERE SHMF_table.SHMF_c_03 = " + "\"" + Scan_Val + "\"" + "AND RZMF_c_02 = " + "\"" + Souko_Code_select + "\"" +  ";", null);
                     */

                    /*
                    Cursor cursor = j_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03 ,SHMF_c_04,SHMF_c_05,t1.RZMF_c_02, t1.RZMF_c_03, SOMF_table.SOMF_c_02, SOMF_table.SOMF_c_03 " +
                    "FROM SHMF_table " +
                    "LEFT OUTER JOIN (SELECT * FROM RZMF_table WHERE RZMF_c_02 = " + "\"" + Souko_Code_select + "\"" + ") t1 " +
                    "ON SHMF_table.SHMF_c_01 = t1.RZMF_c_01 " +
                    "left outer join SOMF_table on SOMF_table.SOMF_c_01 = t1.RZMF_c_02 " +
                    "WHERE SHMF_c_03 =  " + "\"" + Scan_Val + "\"" , null);
                    */

                    Cursor cursor = j_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03 ,SHMF_c_04,SHMF_c_05,t1.RZMF_c_02, t1.RZMF_c_03, SOMF_table.SOMF_c_02, SOMF_table.SOMF_c_03 " +
                            "FROM SHMF_table " +
                            "LEFT OUTER JOIN (SELECT * FROM RZMF_table WHERE RZMF_c_02 = " + "\"" + Souko_Code_select + "\"" + ") t1 " +
                            "ON SHMF_table.SHMF_c_01 = t1.RZMF_c_01 " +
                            "left outer join SOMF_table on SOMF_table.SOMF_c_01 = t1.RZMF_c_02 " +
                            "WHERE SHMF_c_03 =  " + "\"" + Scan_Val + "\"", null);


                    // JAN_count_02
                    JAN_count_02 = cursor.getCount();
                    System.out.println("JAN_count_02:::" + JAN_count_02);

                    if (cursor != null && cursor.moveToFirst()) {

                        do {
                            T_NUM++;
                            JAN_count = cursor.getCount();
                            test_count++;
                            System.out.println("test_count:::" + test_count);
                        } while (cursor.moveToNext());

                    }// ======= END if

                    // === JAN コード検索数
                    System.out.println("cursor.getCount() 01;:::" + cursor.getCount());
                    System.out.println("T_NUM;:::" + T_NUM);
                    System.out.println("JAN_count:::" + JAN_count);

                    /**
                     *  ＝＝＝　取得結果の　カラム数が　１件の場合　＝＝＝
                     */
                    if (JAN_count == 1) {

                        String[] arr_item = new String[7];

                        String edit_qr_num01 = "";
                        String edit_qr_num02 = "";
                        //  String edit_qr_num03 = "";

                        if (cursor != null && cursor.moveToFirst()) {

                            do {

                                // カラム 01
                                int idx = cursor.getColumnIndex("SHMF_c_01"); // 商品C
                                arr_item[0] = cursor.getString(idx);
                                edit_qr_num01 = arr_item[0];

                                Shouhin_Code_Insert = arr_item[0]; // *** 商品コード インサート用

                                // === テキストビュー へ 商品コードを挿入
                                h_moku_text.setText(edit_qr_num01);

                                // カラム 02
                                idx = cursor.getColumnIndex("SHMF_c_02"); // 品名
                                arr_item[1] = cursor.getString(idx);
                                edit_qr_num02 = arr_item[1];

                                Shouhin_Name_Insert = arr_item[1]; // *** 商品名 インサート用

                                // INSERT 用　品目コード
                                SH_col_2 = arr_item[1];
                                System.out.println(SH_col_2 + "SH_col_2　出力テスト");

                                // カラム 03
                                idx = cursor.getColumnIndex("SHMF_c_03"); // JANコード
                                arr_item[2] = cursor.getString(idx);

                                // === テキストビュー へ 商品名 挿入
                                h_moku_m_text.setText(arr_item[1]);

                                // === jan コード表示用
                                jancode_text.setText(arr_item[2]);

                                // カラム 04
                                idx = cursor.getColumnIndex("RZMF_c_02"); // 倉庫C
                                arr_item[3] = cursor.getString(idx);

                                Souko_Code_Insert = arr_item[3]; // *** 倉庫コード インサート用

                                // カラム 05
                                idx = cursor.getColumnIndex("SHMF_c_05"); // 入数
                                arr_item[4] = cursor.getString(idx);

                                // === 入数が０の場合、１を入れる
                                if (arr_item[4].equals("0")) {
                                    arr_item[4] = "1";
                                }

                                // === 入り数　表示用
                                irisuu_text.setText(arr_item[4]);

                                Iri_Num_Insert = arr_item[4]; // *** 入数 インサート用

                                // カラム 06
                                idx = cursor.getColumnIndex("RZMF_c_03"); // 棚番（ロケーション）
                                arr_item[5] = cursor.getString(idx);

                                /**
                                 *  Fucntion:Location_Num_Check ロケーション　チェック
                                 */

                                //    Location_Num_Check(arr_item[5]);

                                // === 商品コード、ロケーションコードをセット
                                Location_Num_Check_02(arr_item[0], arr_item[5]);

                                // 商品コード　出力チェック
                                System.out.println("onActivityResult:::arr_item[0]:::" + arr_item[0]);


                                String Location_Code = arr_item[5];

                                // ロケーションコード
                                //  location_text.setText(Location_Code);

                                // カラム 07
                                idx = cursor.getColumnIndex("SOMF_c_02"); // 倉庫名
                                arr_item[6] = cursor.getString(idx);

                                // === テキストビューへ　倉庫名をセット
                                // h_moku_b_text.setText(arr_item[6]);

                            } while (cursor.moveToNext());

                        }// ======= END if

                        System.out.println("IF １件分岐:::OK =======================");

                        String Tmp_h_moku_text = h_moku_text.getText().toString();
                        // 商品コード　、　倉庫コード　、　商品名が　空だった場合
                        if (Tmp_h_moku_text.isEmpty() && Get_Souko_Code.isEmpty() && edit_qr_num02.isEmpty()) {
                            toastMake("該当の商品はありません。", 0, -200);
                            return;
                        }

                        bara_num_edit.requestFocus();  // バラ数
                        //  case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        // ソフトキーボードを表示する

                        System.out.println(" *********************** バーコード 終了 ***********************");


                    } else if (JAN_count >= 2 || JAN_count != 0) {
                        /**
                         *  ＝＝＝　取得結果の　カラム数が　2件以上の場合　＝＝＝
                         */

                        String[] arr_item = new String[7];

                        String edit_qr_num01 = "";
                        String edit_qr_num02 = "";
                        //  String edit_qr_num03 = "";

                        int loop_count = 0;

                        if (cursor != null && cursor.moveToFirst()) {

                            do {

                                // カラム 01
                                int idx = cursor.getColumnIndex("SHMF_c_01"); // 商品C
                                arr_item[0] = cursor.getString(idx);
                                edit_qr_num01 = arr_item[0];

                                // === テキストビュー へ 商品コードを挿入
                                // h_moku_text.setText(edit_qr_num01);

                                JAN_Shoushi_C_List.add(arr_item[0]); // 重複している JANコードの商品コード 取得

                                // カラム 02
                                idx = cursor.getColumnIndex("SHMF_c_02"); // 品名
                                arr_item[1] = cursor.getString(idx);
                                edit_qr_num02 = arr_item[1];

                                JAN_Shoushi_Name_List.add(arr_item[1]); // 商品名 リスト挿入

                                // INSERT 用　品目コード
                                SH_col_2 = arr_item[1];

                                System.out.println(SH_col_2 + "SH_col_2　出力テスト");

                                // カラム 03
                                idx = cursor.getColumnIndex("SHMF_c_03"); // JANコード
                                arr_item[2] = cursor.getString(idx);

                                GET_sql_Jan_Code = arr_item[2];

                                loop_count++;
                            } while (cursor.moveToNext());

                        }

                        // ====== 配列の要素数を取得して、分岐する
                        int GEt_Zyuufuku_NUM = JAN_Shoushi_C_List.size();
                        System.out.println("GEt_Zyuufuku_NUM:::" + GEt_Zyuufuku_NUM);

                        //--------------- アラートダイアログ　の表示　開始 ---------------------
                        AlertDialog.Builder bilder = new AlertDialog.Builder(ReadBerCode.this);

                        //-------------- カスタムタイトル　作成
                        TextView titleView;
                        titleView = new TextView(ReadBerCode.this);
                        titleView.setText("JANコード"
                                + "【" + GET_sql_Jan_Code + "】" + "\n"
                                + "\n\n" + "重複している商品がありますので、選択してください。");
                        titleView.setTextSize(18);
                        titleView.setTextColor(Color.WHITE);
                        titleView.setBackgroundColor(getResources().getColor(R.color.back_color_01));
                        titleView.setPadding(20, 30, 20, 30);
                        titleView.setGravity(Gravity.CENTER);
                        //-------------- カスタムタイトル　作成 END
                        // ダイアログの項目
                        bilder.setCustomTitle(titleView);

                        /**
                         *  端末のバックボタンを押した時の処理
                         */
                        bilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                                    Allaht_Dialog_Clear();

                                    return true; // イベントを処理済みとしてtrueを返す
                                }
                                return false; // それ以外のキーイベントは通常の処理に任せるためfalseを返す
                            }
                        });

                        /**
                         *  背景以外を押した時の処理
                         */
                        bilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {

                                Allaht_Dialog_Clear();

                                return;

                            }
                        });

                        switch (GEt_Zyuufuku_NUM) {
                            // === JAN で重複レコードが２件だった場合
                            case 2:
                                Shouhin_C_01 = JAN_Shoushi_C_List.get(0);
                                Shouhin_C_02 = JAN_Shoushi_C_List.get(1);

                                Shouhin_Name_01 = JAN_Shoushi_Name_List.get(0);
                                Shouhin_Name_02 = JAN_Shoushi_Name_List.get(1);

                                //-------------- ダイアログ　メッセージ内容
                                /*
                                String msg_bilder = "商品コード 01：" + Shouhin_C_01 + "\n" +
                                        "商品名 01：" + Shouhin_Name_01 + "\n\n" +
                                        "商品コード 02：" +  Shouhin_C_02 + "\n" +
                                        "商品名 02：" + Shouhin_Name_02;
                                bilder.setMessage(msg_bilder);

                                 */

                                // set dialog message
                                bilder.setItems(new CharSequence[]{
                                        "【" + Shouhin_C_01 + "】" + ":" + Shouhin_Name_01 + "\n\n",
                                        "【" + Shouhin_C_02 + "】" + ":" + Shouhin_Name_02 + "\n\n",
                                        "キャンセル"
                                }, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 選択されたアイテムに応じた処理を実行
                                        switch (which) {
                                            case 0:
                                                // Item1 が選択された場合の処理
                                                SELECT_Shouhin_Code(Shouhin_C_01);

                                                Allaht_Dialog_Clear();

                                                break;
                                            case 1:
                                                SELECT_Shouhin_Code(Shouhin_C_02);

                                                Allaht_Dialog_Clear();

                                                break;
                                            case 2:
                                                // Item3 が選択された場合の処理

                                                Allaht_Dialog_Clear();

                                                break;
                                        }
                                    }
                                });

                                dialog = bilder.create();
                                dialog.show();

                                break;

                            // === JAN で重複レコードが、３件だった場合
                            case 3:
                                Shouhin_C_01 = JAN_Shoushi_C_List.get(0);
                                Shouhin_C_02 = JAN_Shoushi_C_List.get(1);
                                Shouhin_C_03 = JAN_Shoushi_C_List.get(2);

                                Shouhin_Name_01 = JAN_Shoushi_Name_List.get(0);
                                Shouhin_Name_02 = JAN_Shoushi_Name_List.get(1);
                                Shouhin_Name_03 = JAN_Shoushi_Name_List.get(2);

                                //-------------- ダイアログ　メッセージ内容
                                msg_bilder = "商品コード 01：" + Shouhin_C_01 + "\n" +
                                        "商品名 01：" + Shouhin_Name_01 + "\n\n" +
                                        "商品コード 02：" + Shouhin_C_02 + "\n" +
                                        "商品名 02：" + Shouhin_Name_02 + "\n\n" +
                                        "商品名 03：" + Shouhin_Name_03 + "\n" +
                                        "商品コード 03：" + Shouhin_C_03 + "\n";
                                bilder.setMessage(msg_bilder);

                                bilder.setPositiveButton(Shouhin_C_01, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Allaht_Dialog_Clear();

                                        return;
                                    }
                                });

                                bilder.setNegativeButton(Shouhin_C_02, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Allaht_Dialog_Clear();

                                        return;
                                    }
                                });

                                bilder.setNeutralButton(Shouhin_C_03, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Button2がクリックされた場合の処理

                                        Allaht_Dialog_Clear();

                                        return;
                                    }
                                });


                                dialog = bilder.create();
                                dialog.show();

                                break;
                        }

                        System.out.println("IF ２件分岐:::OK =======================");

                    } else {
                        /**
                         * 　＝＝＝　０件だった場合の処理　＝＝＝
                         */

                        toastMake("該当の商品はありません。", 0, -200);
                        System.out.println("IF ０件分岐:::OK =======================");

                        Save_init(); //　＊＊＊＊＊＊＊＊＊＊＊ テキストビューなどを　初期化　＊＊＊＊＊＊＊＊＊＊＊

                        return;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (j_db != null) {
                        j_db.close();
                    }
                }

            }

        } // === END if

    } // ----------------------- END






    // --------------------- トーストメッセージ表示 -----------------------
    private void toastMake(String message, int x, int y) {
        Toast toast = Toast.makeText(this,message, Toast.LENGTH_LONG);

        // 位置調整
        toast.setGravity(Gravity.CENTER, x,y);
        toast.show();
    }
    // --------------------- トーストメッセージ表示 END ----------------------->



    // CSV 形式で　（カンマ）　区切りでの　書き込み
    private void Csv_Write() {

        OutputStream out;

        try {
            // MODE_APPEND => 既にファイルがあった場合、追記で開く
            out = openFileOutput("test_r.csv", MODE_PRIVATE | MODE_APPEND);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));

            //------------- 挿入 -------------

            id++;
            writer.append((char) id); // 項目　１　：　id
            writer.print(",");
            System.out.println("CSV　書き込み　項目１：" + id);

            if(gg_accont != null) {
                writer.append(gg_accont); // 項目 ２　：　担当者コード
                writer.print(",");
                System.out.println("CSV　書き込み　項目２：" + gg_accont);
            } else {
                writer.append("null"); // 項目 ２　：　担当者コード
                writer.print(",");
                System.out.println("CSV　書き込み　項目２：" + gg_accont);
            }


            // スピナー　文字列　切り出し
            spi_str = spi_item;

            if(spi_str != null) {
                String sum;
                int idx_1 = spi_str.indexOf(":");
                sum = spi_str.substring(0, idx_1);
                writer.append(sum); // 項目　３　：　部署コード
                System.out.println("CSV　書き込み　項目３：" + sum);
            } else {
                String sum;
                sum = "null";
                writer.append(sum); // 項目　３　：　部署コード
                System.out.println("CSV　書き込み　項目３：" + sum);
            }


            if(br_text != null) {
                writer.append(br_text.getText()); //　項目　４　： 現品票 コード
                writer.print(",");
                System.out.println("CSV　書き込み　項目４：" + br_text.getText());
            } else {
                writer.append("null");
                writer.print(",");
                System.out.println("CSV　書き込み　項目４：" + "null");
            }

            if(h_moku_text != null) {
                writer.append(h_moku_text.getText()); // 品目コード
                writer.print(",");
                System.out.println("CSV　書き込み　項目５：" + h_moku_text.getText());
            } else {
                writer.append("null");
                writer.print(",");
                System.out.println("CSV　書き込み　項目５：" + "null");
            }

            if(h_moku_m_text != null) {
                writer.append(h_moku_m_text.getText()); // 品7目名称コード
                writer.print(",");
                System.out.println("CSV　書き込み　項目６：" + h_moku_text.getText());
            } else {
                writer.append("null");
                writer.print(",");
                System.out.println("CSV　書き込み　項目６：" + "null");
            }


            if(h_moku_b_text != null) {
                writer.append(h_moku_b_text.getText()); // 品目備考
                writer.print(",");
            } else {
                writer.append("null");
                writer.print(",");
                System.out.println("CSV　書き込み　項目７：" + "null");
            }

            // 棚卸個数 整数に変換
            item_num = Integer.parseInt(tmp_tanasuu);
            writer.print(item_num); // 棚卸数
            System.out.println("CSV　書き込み　項目８：" + item_num);
            writer.print(",");

            writer.append(SH_col_6); // 場所
            System.out.println("CSV　書き込み　項目９：" + SH_col_6);
            writer.println();

            count_num++; // 件数

            System.out.print("データ書き込み　完了");

            System.out.println(count_num);

            if(writer != null) {
                writer.close();
                String str_02 = "保存完了しました。";
                toastMake(str_02,0,-200);
            }

        } catch (IOException e) {
            // 例外処理
            e.printStackTrace();
        }

    } //-------------------------- Csv_Write END


    /***
     *   品目　
     */

    /*
    private void Spiiner_03() {

        // ヘルパー　メソッド
        helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] arr_item = new String[2];

        ArrayList<String> Item_tb_item = new ArrayList<>();

        try {

            Cursor cursor = db.rawQuery("SELECT * from Item_tb;", null);

            while(cursor.moveToNext()) {

                int idx = cursor.getColumnIndex("item_tb_c_01");
                arr_item[0] = cursor.getString(idx);

                idx = cursor.getColumnIndex("item_tb_c_02");
                arr_item[1] = cursor.getString(idx);

                // 品目コード　テーブル　値取得
                Item_tb_item.add(arr_item[0] + ":" + arr_item[1]);

                //------ 比較用　ハッシュマップに　格納 ------
                spi_map.put(arr_item[0], arr_item[1]);

            }

            for(Map.Entry<String, String> entry : spi_map.entrySet()) {
                System.out.println("spi_map マップキー" + entry.getKey() + " : " + entry.getValue());
            }



        } finally {
            if(db != null) {
                db.close();
            }
        }

    }
     */




    private void Write_Send_01() {

        // データ挿入　変数
        int max_id = 0;
        String [] arr_data = new String [9];

        try {
            //---------- Send_db インサート

           values = new ContentValues();

            //------ max id (件数)　取得
            String tmp = max_data_view.getText().toString();
            int id = Integer.parseInt(tmp);
            values.put(TestOpenHelper.SEND_DB_C_01, id + 1);

            //------ 2件目 アカウント　番号
            values.put(TestOpenHelper.SEND_DB_C_02, gg_accont);

            //------ 3件目 部署コード スピンナー　切り出し
            spi_str = spi_item;

            if(spi_str != null) {
                String sum;
                int idx_1 = spi_str.indexOf(":");
                sum = spi_str.substring(0, idx_1);
                values.put(TestOpenHelper.SEND_DB_C_03, sum); // 項目　３　：　部署コード
                System.out.println("インサート 書き込み　項目３：" + sum);
            } else {
                String sum;
                sum = "null";
                values.put(TestOpenHelper.SEND_DB_C_03, sum); // 項目　３　：　部署コード
                System.out.println("インサート　書き込み　項目３：" + sum);
            }

            //------- 4 件目 現品票コード
            String get_04 = br_text.getText().toString();
            values.put(TestOpenHelper.SEND_DB_C_04, get_04);

            //------- 5 件目　品目コード
            String get_05 = h_moku_text.getText().toString();
            values.put(TestOpenHelper.SEND_DB_C_05, get_05);

            //------- 6 件目 品目名称
            String get_06 = h_moku_m_text.getText().toString();
            values.put(TestOpenHelper.SEND_DB_C_06, get_06);

            //------- 7 件目 品目備考
            String get_07 = h_moku_b_text.getText().toString();
            values.put(TestOpenHelper.SEND_DB_C_07, get_07);

            //------- 8 件目　棚卸し数 tmp_tanasuu
            values.put(TestOpenHelper.SEND_DB_C_08, tmp_tanasuu);

            //------- 9 件目
            values.put(TestOpenHelper.SEND_DB_C_09, "null");

            db.insert("Send_db", null, values);
            System.out.println("Send_db テーブル DB １行　インサート　成功");

            db.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Send_db テーブル DB １行　インサート　エラー SQLException");

        }


    }



    /**
     * 初期値設定 (EditTextの入力欄は空白、※印は消す)
     * init()
     */
    private void init() {

        // エディットテキスト

        // テキストビュー
        qr_edit.setText("");
        h_moku_text.setText("");
        h_moku_m_text.setText("");
      //   h_moku_b_text.setText("");
      //  h_moku_text_area.setText("");  //

      //  location_text.setText(""); // 在庫場所

        bara_num_edit.requestFocus(); // バラ数
   //     case_num_edit.requestFocus();      // フォーカスをEditTextに指定

        /**
         *  東北用
         */
        jancode_text.setText(""); // JAN コード
        irisuu_text.setText(""); // 入数

        case_num_edit.setText(""); // ケース数
        case_sum_text.setText(""); // ケース数　表示
        bara_num_edit.setText(""); // バラ数
        tana_sum_text.setText(""); // 棚卸し 合計数

        JAN_count = 0;
    }

    /**
     *  インサートの後の、画面初期化
     */
    private void Save_init(){

        // エディットテキスト

        // テキストビュー
        qr_edit.setText("");
        h_moku_text.setText("");
        h_moku_m_text.setText("");
        //   h_moku_b_text.setText("");
        //  h_moku_text_area.setText("");  //

        //  location_text.setText(""); // 在庫場所

        // case_num_edit.requestFocus();      // フォーカスをEditTextに指定

        /**
         *  東北用
         */
        jancode_text.setText(""); // JAN コード
        irisuu_text.setText(""); // 入数

        case_num_edit.setText(""); // ケース数
        case_sum_text.setText(""); // ケース数　表示
        bara_num_edit.setText(""); // バラ数
        tana_sum_text.setText(""); // 棚卸し 合計数

        JAN_count = 0;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    /**
     * EditTextに入力したテキストをDBに登録
     * saveDB()
     */

    private void saveList() {

        /**
         *  ========== 日付取得 ・　時間取得  ==========
         */
        Now_date_str = getNowDate();
        get_year = Now_date_str.substring(0,4); //yyyy
        get_month = Now_date_str.substring(4,6); // MM
        get_day = Now_date_str.substring(6,8); // dd

        get_hour = Now_date_str.substring(8, 10); //  １時間
        get_minute = Now_date_str.substring(10, 12); //　１分
        get_second = Now_date_str.substring(12, 14); // 1秒

        // === インサート用　当日日付
        Now_date_str_Insert = get_year + "/" + get_month + "/" + get_day + " " + get_hour +
                ":" + get_minute + ":" + get_second;

        /**
         *  ========== 日付取得 ・　時間取得 END  ==========
         */

        /*
        String get_col_text_05 = tana_edit.getText().toString(); // 棚卸し数 : 9

        String br_text_num = qr_edit.getText().toString();  // 現品票バーコード : 4
        String h_moku_text_num = h_moku_text.getText().toString(); // 品目コード : 5 スピナーに変える
        String h_moku_text_area_num = h_moku_text_area.getText().toString(); // 項目： 8  品目コード (TextView) ※　値変えれる
        String h_moku_m_text_num = h_moku_m_text.getText().toString(); // 品目名称 : 7
        String h_moku_b_text_num = h_moku_b_text.getText().toString(); // 品目備考 : 8

         */
     //   String location_text_num = location_text.getText().toString(); // 在庫場所 : 10

        try {

            /**
             *  ======================= 東北用　処理 ============================
             */

            /**
             *  東北支店 用
             */

            Shouhin_Code_Insert = h_moku_text.getText().toString(); // 商品コード
            Shouhin_Name_Insert = h_moku_m_text.getText().toString(); // 商品名
            Iri_Num_Insert = irisuu_text.getText().toString(); // 入数

            String User_num_02 = user_num_02.getText().toString();
            Tana_Num_Insert = tana_sum_text.getText().toString(); // === 合計数


            String Tmp_Souko_Code = souko_name.getText().toString();
            Souko_Code_Insert = GET_SELECT_Souko_ID(Tmp_Souko_Code); // 倉庫 コード取得

            String Tmp_Shouhin_Name = h_moku_m_text.getText().toString();
          //  Shouhin_Code_Insert = GET_SELECT_Shouhinn_Code(Tmp_Shouhin_Name); // 商品コード　取得

            String Tmp_Location_Code = location_name.getText().toString();
            Location_Code_Insert = Tmp_Location_Code; // ロケーションコード　取得

            // バーコード読み取り時  品区 取得
            br_val_01_num = br_val_01;
            System.out.println("br_val_01_num 出力テスト" + br_val_01_num);


            // === テスト出力
            System.out.println("インサート前テスト出力 Account_Id:::" + Account_Id);
            System.out.println("インサート前テスト出力 Souko_Code_Insert:::" + Souko_Code_Insert);
            System.out.println("インサート前テスト出力 Shouhin_Code_Insert:::" + Shouhin_Code_Insert);
            System.out.println("インサート前テスト出力 Shouhin_Name_Insert:::" + Shouhin_Name_Insert);
            System.out.println("インサート前テスト出力 Location_Code_Insert:::" + Location_Code_Insert);
            System.out.println("インサート前テスト出力 Iri_Num_Insert:::" + Iri_Num_Insert);
            System.out.println("インサート前テスト出力 Tana_Num_Insert:::" + Tana_Num_Insert);
            System.out.println("インサート前テスト出力 Now_date_str_Insert:::" + Now_date_str_Insert);
            System.out.println("インサート前テスト出力 Get_ID:::" + Get_ID);

            String Tanmatu_ID = Get_ID.replace("端末ID：","");

            //------- 棚卸し数が　空の　場合の処理 ------>
            if(Tana_Num_Insert.equals("") || Tana_Num_Insert.isEmpty()) {

                toastMake("棚卸し数が空です。", 0,-200);

            } else {
                // エディットテキストが　空じゃ　ない場合

                // 担当者コード　処理 項目 :2
                if (User_num_02 == null || User_num_02.equals("") || User_num_02.isEmpty()) {
                    User_num_02 = "未登録ユーザー";
                }


                //---------- その他の項目の　NUll チェック ------------//

                br_val_01_num = br_val_01; // 品区 取得

                // DB への　登録
                DBAdapter dbAdapter = new DBAdapter(this);
                dbAdapter.openDB();

                // DBの読み書き
                // gg_accont = 担当者コード, inse_Comp_01_num = 部署コード（管理課）　加工済みデータ => SH_col_7 (場所コード　加工前 B0886) ,  br_text_num = 現品票バーコード, br_val_01_num = 品区 SH_col_2_num = 品目コード   ,     9: SH_col_7_num =　在庫場所

                // 1:担当者コード => Account_Id , 2:倉庫コード => Souko_Code_Insert , 3:商品コード => Shouhin_Code_Insert , 4:商品名 => Shouhin_Name_Insert
                // 5:棚番（ロケ） => Location_Code_Insert, 6:入数 => Iri_Num_Insert  0 だったら 1　を入れる, 7:数量 => Tana_Num_Insert,
                // 8:開始日時  , 9:端末名 => Get_ID
                dbAdapter.saveDB(Account_Id, Souko_Code_Insert, Shouhin_Code_Insert, Shouhin_Name_Insert, Location_Code_Insert, Iri_Num_Insert, Tana_Num_Insert, Now_date_str_Insert, Tanmatu_ID);   // DBに登録

                System.out.println("インサート　完了");

                dbAdapter.closeDB();                                        // DBを閉じる

                Save_init(); //　＊＊＊＊＊＊＊＊＊＊＊ テキストビューなどを　初期化　＊＊＊＊＊＊＊＊＊＊＊

                try {
                    Thread.sleep(500); // ミリ秒単位で待機時間を指定

                    // カメラ起動
                    integrator = new IntentIntegrator(ReadBerCode.this);
                    // キャプチャ画面の下方にメッセージを表示
                    integrator.setPrompt("戻る　ボタン タップで「キャンセル」できます。");

                    // キャプチャ画面起動
                    integrator.initiateScan();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        } catch (NullPointerException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    } //------------ END saveList ------->


    /*-------------- findeVIewById 紐づけよう 関数 ---------------------*/
    private void findViews() {

        //---------- 画面　全体を　取得 ----------//
        mainLayout_ber = (ScrollView) findViewById(R.id.mainLayout_ber);

        //---------- header ----------//
        back_btn_02 = (ImageButton) findViewById(R.id.back_btn_02);
        user_num_02 = (TextView) findViewById(R.id.user_num_02);


        //---------- header  END ----------//

        //---------- コンテンツ部分 ----------//
        //------- ボタン
    //    reade_br_btn = (Button) findViewById(R.id.reade_br_btn);

        //------- 表示用ラベル　&　テキストビュー
        h_moku_m_label = (TextView) findViewById(R.id.h_moku_m_label);

      //  br_text = (TextView) findViewById(R.id.br_text); // Jan コード (現品票コード) 　項目 4

        h_moku_text = (TextView) findViewById(R.id.h_moku_text); // 項目 5 品目区分 スピナー ※　値変えれる
//        h_moku_text_area = (TextView) findViewById(R.id.h_moku_text_area); // 項目: 8  品目コード (TextView) ※　値変えれる　（例）　009850GUBK

        // 品目名称　項目:7  （例）　プラスチック収納庫　１７３
        h_moku_m_text = (TextView) findViewById(R.id.h_moku_m_text);


        //　在庫場所 項目: 10
       // locashin_label = (TextView) findViewById(R.id.locashin_label);
       // location_text = (TextView) findViewById(R.id.location_text);

        //---------- コンテンツ部分 END ----------//

        //---------- ボトム　コンテンツ部分 Start ----------//
        //------- エディットテキスト


        //------- ボタン
        sql_read = (Button) findViewById(R.id.sql_read);
        sql_load = (Button) findViewById(R.id.sql_load);

        //---------- ボトム　コンテンツ部分 END ----------//


        //------ QR コード　取得　編集用　エディットテキスト ------------
        qr_edit = (EditText) findViewById(R.id.qr_edit);

        //-------- エディットテキスト　フォーカス　外し用 Veiw ----------
        focusView_01 = (TextView) findViewById(R.id.focusView_01);

        /**
         *  東北　倉庫、ロケーション
         */
        souko_name = findViewById(R.id.souko_name); // 倉庫名　表示用
        location_name = findViewById(R.id.location_name); // ロケーション　表示用
        jancode_text = findViewById(R.id.jancode_text); // janコード　表示用
        irisuu_text = findViewById(R.id.irisuu_text); // 入り数　表示用

        // ========= ケース数
        case_num_edit = findViewById(R.id.case_num_edit); // ケース数　入力
        case_sum_text = findViewById(R.id.case_sum_text); // ケース数　合計

        // ========= バラ数
        bara_num_edit = findViewById(R.id.bara_num_edit); // バラ数 入力

        // ========= 棚卸し数　合計
        tana_sum_text = findViewById(R.id.tana_sum_text); // 棚卸し数　合計　テキストビュー

    }


    /**
     *  商品コード　から　SELECT
     */
    private void SELECT_Shouhin_Code(String Shouhin_Code) {

        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase Sh_db = helper.getReadableDatabase();

        String[] arr_item = new String[7];

        String edit_qr_num01 = "";
        String edit_qr_num02 = "";
        //  String edit_qr_num03 = "";

        try {

            Cursor cursor = Sh_db.rawQuery("SELECT SHMF_c_01, SHMF_c_02, SHMF_c_03, SHMF_c_04,SHMF_c_05,RZMF_c_02,RZMF_c_03,SOMF_c_02,SOMF_c_03 FROM SHMF_table " +
                    "left outer join RZMF_table on SHMF_table.SHMF_c_01 = RZMF_table.RZMF_c_01 " +
                    "left outer join SOMF_table on SOMF_table.SOMF_c_01 = RZMF_table.RZMF_c_02 " +
                    "WHERE SHMF_table.SHMF_c_01 = " + "\"" + Shouhin_Code + "\"" + ";", null);

            if(cursor.getCount() <= 0 ) {
                toastMake("該当の商品はありません。",0, -200);
                Save_init(); //　＊＊＊＊＊＊＊＊＊＊＊ テキストビューなどを　初期化　＊＊＊＊＊＊＊＊＊＊＊

                System.out.println("cursor.getCount() ２次元バーコード分岐:::該当なし");
                return;
            }

            if (cursor.moveToNext()) {

                // カラム 01
                int idx = cursor.getColumnIndex("SHMF_c_01"); // 商品C
                arr_item[0] = cursor.getString(idx);
                edit_qr_num01 = arr_item[0];

                // === テキストビュー へ 商品コードを挿入
                h_moku_text.setText(edit_qr_num01);

                // カラム 02
                idx = cursor.getColumnIndex("SHMF_c_02"); // 品名
                arr_item[1] = cursor.getString(idx);
                edit_qr_num02 = arr_item[1];

                // INSERT 用　品目コード
                SH_col_2 = arr_item[1];

                System.out.println(SH_col_2 + "SH_col_2　出力テスト");


                // カラム 03
                idx = cursor.getColumnIndex("SHMF_c_03"); // JANコード
                arr_item[2] = cursor.getString(idx);

                // === JAN コード空 処理
                if(arr_item[2].equals("")) {
                    jancode_text.setText("JANコード マスターなし");
                } else {
                    jancode_text.setText(arr_item[2]); // JANコード 表示用
                }

                // === テキストビュー へ 商品名 挿入
                h_moku_m_text.setText(arr_item[1]);

                // カラム 04
                idx = cursor.getColumnIndex("SHMF_c_04"); // 倉庫C
                arr_item[3] = cursor.getString(idx);

                Get_Souko_Code = arr_item[3]; // === 倉庫コード 格納

                // カラム 05
                idx = cursor.getColumnIndex("SHMF_c_05"); // 入数
                arr_item[4] = cursor.getString(idx);

                // === 入数　０、"" 処理
                if(arr_item[4].equals("0") || arr_item[4].equals("")) {
                    arr_item[4] = "1";
                }

                irisuu_text.setText(arr_item[4]); // 入り数　表示用

                // カラム 06
                idx = cursor.getColumnIndex("RZMF_c_03"); // 棚番（ロケーション）
                arr_item[5] = cursor.getString(idx);

                /**
                 *  Fucntion:Location_Num_Check ロケーション　チェック
                 */
                // 商品コードと、ロケーションコードをセット
                Location_Num_Check_02(arr_item[0], arr_item[5]);

                String Location_Code = arr_item[5];

                // ロケーションコード
                //  location_text.setText(Location_Code);

                // カラム 07
                idx = cursor.getColumnIndex("SOMF_c_02"); // 倉庫名
                arr_item[6] = cursor.getString(idx);

                // === テキストビューへ　倉庫名をセット
                // h_moku_b_text.setText(arr_item[6]);

            }

            bara_num_edit.requestFocus();
          //  case_num_edit.requestFocus(); // EditTextにフォーカスを移動
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(Sh_db != null) {
                Sh_db.close();
            }
        }

    }

    /**
     *  ケース数、バラ数、空に初期化
     */
    private void Case_Bara_Num_Init() {
        case_num_edit.setText(""); // ケース数
        case_sum_text.setText(""); // ケース数　合計
        bara_num_edit.setText(""); // バラ数
        tana_sum_text.setText(""); // 棚卸し 合計数
    }

    /**
     *  入力している ロケーションと、QR, バーコード, 手打ち入力で取得したロケーションが違った場合アラートを出す
     */
    private void Location_Num_Check(String Get_Loca_Num){

        String Input_Loca = location_name.getText().toString();
        if(Input_Loca.isEmpty() || Input_Loca.equals("")) {

        } else {
            if(!(Input_Loca.equals(Get_Loca_Num))) {

                //******************** オリジナルアラートログの表示 処理 開始  ********************//
                LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View bilde_layout_02 = inflater.inflate(R.layout.dialog,(ViewGroup)findViewById(R.id.alertdialog_layout_01));

                //*********** コンポーネント　初期化

                dia_chack_01_01_edit = bilde_layout_02.findViewById(R.id.dia_chack_01_01_edit); // 時間入力
                dia_chack_01_01_edit.setFocusable(true);
                dia_chack_01_01_edit.setFocusableInTouchMode(true);
                dia_chack_01_01_edit.setEnabled(true);
                //****** エディットテキスト　ナンバー入力設定
                dia_chack_01_01_edit.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

                dia_touroku_btn_001 = bilde_layout_02.findViewById(R.id.dia_touroku_btn_001); // 登録ボタン
                dia_touroku_btn_002 = bilde_layout_02.findViewById(R.id.dia_touroku_btn_002); // キャンセルボタン



                // === 説明テキスト
                location_check_setumei = bilde_layout_02.findViewById(R.id.location_check_setumei);


                //--------------- アラートダイヤログ タイトル　設定 ---------------//
                AlertDialog.Builder bilder = new AlertDialog.Builder(ReadBerCode.this);
                // タイトル
                TextView titleView;
                titleView = new TextView(ReadBerCode.this);
                titleView.setText("ロケーションチェック");
                titleView.setTextSize(22);
                titleView.setTextColor(Color.WHITE);
                titleView.setBackgroundColor(getResources().getColor(R.color.colorPinku));
                titleView.setPadding(20, 30, 20, 30);
                titleView.setGravity(Gravity.CENTER);

                // ダイアログに　「タイトル」　を　セット
                bilder.setCustomTitle(titleView);

                // カスタムレイアウト　を　セット
                bilder.setView(bilde_layout_02);

                location_check_setumei.setText("入力されたロケーション" + "【" + Input_Loca + "】" + "\n" +
                        "マスターで取得したロケーション" + "【" + Get_Loca_Num + "】" + "\n" +
                        "の値が違います。入力されたロケーション番号を修正しますか？");

                AlertDialog dialog = bilder.create();
                dialog.show();

                dia_chack_01_01_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                        //****** ソフトキーボードが押されたら
                        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                              String Get_Dialog_Loca_Tmp = dia_chack_01_01_edit.getText().toString();

                              if(Get_Dialog_Loca_Tmp.isEmpty() || Get_Dialog_Loca_Tmp.equals("")) {
                                  return false;
                              } else {
                                  Get_Dialog_Loca = Get_Dialog_Loca_Tmp;
                              }

                        }

                        dialog.dismiss();

                        return false;
                    }
                });

                /**
                 *  ロケーションcheck　ダイアログ（変更）ボタン
                 */
                dia_touroku_btn_001.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Get_Dialog_Loca = dia_chack_01_01_edit.getText().toString();

                        if(Get_Dialog_Loca.isEmpty() || Get_Dialog_Loca.equals("")) {

                            //*********** キャンセル
                            dialog.dismiss();

                        } else {
                            /**
                             *  ダイアログで、ロケーションコード変更処理
                             */
                            location_name.setText(Get_Dialog_Loca);
                            toastMake("ロケーションコードを変更しました。", 0, -200);

                            bara_num_edit.requestFocus();
                         //   case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                            dialog.dismiss();
                        }

                    }
                });

                /**
                 *  ロケーションcheck　ダイアログ（キャンセル）ボタン
                 */
                dia_touroku_btn_002.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        bara_num_edit.requestFocus();
            //            case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                        //*********** キャンセル
                        dialog.dismiss();
                    }
                });


            }

        }

    }

    /**
     *  入力している ロケーションと、QR, バーコード, 手打ち入力で取得したロケーションが違った場合アラートを出す
     *
     *  商品コードで、SELECTして、倉庫コードが NULLまたは 空 じゃなかったらチェックする。
     */
    private void Location_Num_Check_02(String Shouhin_C, String Get_Loca_Num) {

        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] arr_item = new String[1];
        String Return_Loca_Code = "";

        try {
            Cursor cursor = db.rawQuery("SELECT RZMF_c_03 FROM RZMF_table WHERE RZMF_c_01 = " + "\"" + Shouhin_C + "\"" + ";", null);

            if (cursor.moveToFirst()) {
                do {
                    int idx = cursor.getColumnIndex("RZMF_c_03"); // 倉庫コード
                    arr_item[0] = cursor.getString(idx);
                    Return_Loca_Code = arr_item[0]; // === 倉庫コード　取得
                } while (cursor.moveToNext()); //------ END while

                System.out.println("Location_Num_Check_02:::Return_Loca_Code" + Return_Loca_Code);
            } //------ END if

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (db != null) {
                db.close();
            }

        }

        /**
         *  倉庫コードチェック
         */
        // === 入力した、ロケーションコード
        String Input_Loca = location_name.getText().toString();

        // ======= ロケーションコード（入力）が空の場合はチェックしない
        if (Input_Loca.isEmpty() || Input_Loca.equals("")) {
            return;
        }

        System.out.println("Location_Num_Check_02:::Input_Loca::" + Input_Loca);
        System.out.println("Location_Num_Check_02:::Get_Loca_Num::" + Get_Loca_Num);
        if(Return_Loca_Code.isEmpty() || Return_Loca_Code.equals("")) {

            return;
        } else {

            /**
             *  入力された倉庫コードと、マスターの倉庫コードが同じ場合は、return
             */

            System.out.println("Location_Num_Check_02:::Return_Loca_Code" + Return_Loca_Code);
            System.out.println("Location_Num_Check_02:::location_name" + Input_Loca);
            if(Return_Loca_Code.equals(Input_Loca) || Return_Loca_Code == Input_Loca) {
                return;
            } else {
                /**
                 *  入力と、マスターの倉庫コードが違う場合は、変更チェック
                 */

                //******************** オリジナルアラートログの表示 処理 開始  ********************//
                LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View bilde_layout_02 = inflater.inflate(R.layout.dialog,(ViewGroup)findViewById(R.id.alertdialog_layout_01));

                //*********** コンポーネント　初期化

                dia_chack_01_01_edit = bilde_layout_02.findViewById(R.id.dia_chack_01_01_edit); // 時間入力
                dia_chack_01_01_edit.setFocusable(true);
                dia_chack_01_01_edit.setFocusableInTouchMode(true);
                dia_chack_01_01_edit.setEnabled(true);
                //****** エディットテキスト　ナンバー入力設定
                dia_chack_01_01_edit.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

                dia_touroku_btn_001 = bilde_layout_02.findViewById(R.id.dia_touroku_btn_001); // 登録ボタン
                dia_touroku_btn_002 = bilde_layout_02.findViewById(R.id.dia_touroku_btn_002); // キャンセルボタン



                // === 説明テキスト
                location_check_setumei = bilde_layout_02.findViewById(R.id.location_check_setumei);


                //--------------- アラートダイヤログ タイトル　設定 ---------------//
                AlertDialog.Builder bilder = new AlertDialog.Builder(ReadBerCode.this);
                // タイトル
                TextView titleView;
                titleView = new TextView(ReadBerCode.this);
                titleView.setText("ロケーションチェック");
                titleView.setTextSize(22);
                titleView.setTextColor(Color.WHITE);
                titleView.setBackgroundColor(getResources().getColor(R.color.colorPinku));
                titleView.setPadding(20, 30, 20, 30);
                titleView.setGravity(Gravity.CENTER);

                // ダイアログに　「タイトル」　を　セット
                bilder.setCustomTitle(titleView);

                // カスタムレイアウト　を　セット
                bilder.setView(bilde_layout_02);

                location_check_setumei.setText("入力されたロケーション" + "【" + Input_Loca + "】" + "\n" +
                        "マスターで取得したロケーション" + "【" + Get_Loca_Num + "】" + "\n" +
                        "の値が違います。入力されたロケーション番号を修正しますか？");

                AlertDialog dialog = bilder.create();
                dialog.show();

                dia_chack_01_01_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                        //****** ソフトキーボードが押されたら
                        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                            String Get_Dialog_Loca_Tmp = dia_chack_01_01_edit.getText().toString();

                            if(Get_Dialog_Loca_Tmp.isEmpty() || Get_Dialog_Loca_Tmp.equals("")) {
                                return false;
                            } else {
                                Get_Dialog_Loca = Get_Dialog_Loca_Tmp;
                            }

                        }

                        dialog.dismiss();

                        return false;
                    }
                });

                /**
                 *  ロケーションcheck　ダイアログ（変更）ボタン
                 */
                dia_touroku_btn_001.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Get_Dialog_Loca = dia_chack_01_01_edit.getText().toString();

                        if(Get_Dialog_Loca.isEmpty() || Get_Dialog_Loca.equals("")) {

                            //*********** キャンセル
                            dialog.dismiss();

                        } else {
                            /**
                             *  ダイアログで、ロケーションコード変更処理
                             */
                            location_name.setText(Get_Dialog_Loca);
                            toastMake("ロケーションコードを変更しました。", 0, -200);

                            bara_num_edit.requestFocus();
                      //      case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                            dialog.dismiss();
                        }

                    }
                });

                /**
                 *  ロケーションcheck　ダイアログ（キャンセル）ボタン
                 */
                dia_touroku_btn_002.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        bara_num_edit.requestFocus();
                 //       case_num_edit.requestFocus(); // EditTextにフォーカスを移動
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                        //*********** キャンセル
                        dialog.dismiss();
                    }
                });

            }
        }

    }


    /**
     * 現在日時をyyyy/MM/dd HH:mm:ss形式で取得する.
     */
    public static String getNowDate() {

        final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        final Date date = new Date(System.currentTimeMillis());

        return df.format(date);
    }

    /**
     *   倉庫 ID を取得
     */
    public String GET_SELECT_Souko_ID(String Souko_Name) {

        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] arr_item = new String[1];
        String Retunr_Souko_ID = "";
        try {

            Cursor cursor = db.rawQuery("SELECT SOMF_c_01  FROM SOMF_table WHERE SOMF_c_02 = " + "\"" + Souko_Name + "\"" + ";", null);

            if (cursor.moveToFirst()) {
                do {
                    int idx = cursor.getColumnIndex("SOMF_c_01"); // 倉庫コード
                    arr_item[0] = cursor.getString(idx);
                    Retunr_Souko_ID = arr_item[0];

                } while (cursor.moveToNext()); //------ END while
            } //------ END if

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (db != null) {
                db.close();
            }

        }

        return Retunr_Souko_ID;
    }

    /**
     *   商品 ID を取得
     */
    public String GET_SELECT_Shouhinn_Code(String Shouhinn_Name) {

        TestOpenHelper helper = new TestOpenHelper(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] arr_item = new String[1];
        String Return_Shouhin_Code = "";

        try {
            Cursor cursor = db.rawQuery("SELECT SHMF_c_01 from SHMF_table WHERE SHMF_c_02 = " + "\"" + Shouhinn_Name + "\"" + ";", null);

            if (cursor.moveToFirst()) {
                do {
                    int idx = cursor.getColumnIndex("SHMF_c_01"); // 倉庫コード
                    arr_item[0] = cursor.getString(idx);
                    Return_Shouhin_Code = arr_item[0];
                } while (cursor.moveToNext()); //------ END while
            } //------ END if

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (db != null) {
                db.close();
            }

        }

        return Return_Shouhin_Code;
    }

    /**
     *  アラートダイアログ　クリア時の処理
     */
    private void Allaht_Dialog_Clear() {

        JAN_Shoushi_C_List.clear();
        JAN_Shoushi_Name_List.clear();
        Shouhin_C_01 = "";
        Shouhin_C_02 = "";
        Shouhin_Name_01 = "";
        Shouhin_Name_02 = "";

        // === アラートダイアログを閉じる
        dialog.dismiss();
    }


}
