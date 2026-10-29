package com.profileforge.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.widget.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.graphics.Typeface;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.io.File;
import java.io.FileOutputStream;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Build;

public class MainActivity extends Activity {
    final int BG = Color.rgb(9,10,16), PANEL = Color.rgb(17,19,29), LINE = Color.rgb(39,41,58);
    final int TEXT = Color.rgb(246,245,255), MUTED = Color.rgb(151,151,174), PURPLE = Color.rgb(139,92,246), GREEN = Color.rgb(167,243,208);
    LinearLayout root, content, nav;
    EditText endingInput, secretInput;
    Spinner genderSpinner, countSpinner;
    LinearLayout resultList;
    WebView mailWeb;
    TextView otpView, otpStatus, otpTimer;
    HandlerTimer timer;
    final SecureRandom random = new SecureRandom();
    final Set<String> usernames = new HashSet<>();
    final ArrayList<Profile> profiles = new ArrayList<>();
    String currentTab = "profiles";
    final String mailUrl = "https://dongvanfb.net/read_mail_box/";

    static class HandlerTimer {
        android.os.Handler handler = new android.os.Handler();
        Runnable runnable;
        void stop(){ if(runnable!=null) handler.removeCallbacks(runnable); }
    }
    static class Profile {
        String name, username, password;
        Profile(String n,String u,String p){name=n;username=u;password=p;}
    }

    final String[] maleNames = {"Tanvir","Fahim","Rakib","Sakib","Shakib","Nayeem","Imran","Rafi","Arafat","Mehedi","Hasan","Mahmud","Nabil","Siam","Tamim","Riad","Nahid","Arif","Tawhid","Shuvo","Rashed","Jubayer","Farhan","Adnan","Sajid","Ashik","Rony","Bappy","Muntasir","Zubair"};
    final String[] femaleNames = {"Sadia","Nusrat","Sumaiya","Tasnim","Jannat","Mim","Nabila","Tanjila","Maliha","Anika","Raisa","Ayesha","Faria","Sanjida","Ishrat","Samia","Tania","Rumana","Lamisa","Mahi","Nafisa","Sharmeen","Afia","Rukaiya","Orin","Sohana","Mehjabin","Afsana","Jerin","Raisa"};
    final String[] surnames = {"Rahman","Ahmed","Hossain","Islam","Hasan","Khan","Chowdhury","Akter","Begum","Uddin","Miah","Sarker","Mahmud","Karim","Alam","Kabir","Molla","Sikder","Talukder","Bhuiyan","Mondal","Das","Roy","Saha","Paul","Biswas","Ghosh","Dutta","Basu","Chakraborty"};
    final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildShell();
        showProfiles();
    }

    int dp(float x){ return (int)(x*getResources().getDisplayMetrics().density+0.5f); }
    GradientDrawable bg(int color,int stroke,int radius){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,lp(1,h));}
    Button button(String label,boolean primary,Runnable action){
        Button b=new Button(this);b.setText(label);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(TEXT);
        b.setBackground(bg(primary?PURPLE:Color.rgb(28,30,44),primary?Color.rgb(157,124,255):LINE,11));
        b.setPadding(dp(10),dp(4),dp(10),dp(4)); b.setLetterSpacing(0.015f); b.setElevation(primary?dp(2):0); b.setOnClickListener(v->action.run());return b;
    }
    EditText edit(String hint,String value){
        EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(TEXT);e.setTextSize(14);
        e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackground(bg(Color.rgb(11,14,24),LINE,10));return e;
    }
    void buildShell(){
        root=column();root.setBackgroundColor(BG);root.setPadding(dp(16),dp(12),dp(16),dp(8));
        setContentView(root);
        TextView logo=text("✦  PROFILEFORGE",21,TEXT,true);logo.setGravity(Gravity.CENTER);logo.setLetterSpacing(0.08f);root.addView(logo,lp(-1,44));
        TextView sub=text("PRIVATE PROFILE WORKSPACE",10,MUTED,true);sub.setGravity(Gravity.CENTER);sub.setLetterSpacing(0.16f);root.addView(sub,lp(-1,23));
        View accent=new View(this);accent.setBackground(bg(PURPLE,0,2));LinearLayout.LayoutParams ap=lp(42,3);ap.gravity=Gravity.CENTER;ap.topMargin=dp(5);ap.bottomMargin=dp(8);root.addView(accent,ap);
        nav=row();nav.setPadding(dp(3),dp(3),dp(3),dp(3));nav.setBackground(bg(Color.rgb(15,16,25),LINE,14));root.addView(nav,lp(-1,52));
        addNav("Profiles","profiles");addNav("Mail Verification","mail");addNav("2FA","twofa");
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=column();content.setPadding(0,dp(12),0,dp(20));scroll.addView(content);
    }
    void addNav(String label,String tab){
        Button b=button(label,tab.equals(currentTab),()->switchTab(tab));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(44),1);p.setMargins(dp(2),0,dp(2),0);nav.addView(b,p);
    }
    void switchTab(String tab){currentTab=tab;nav.removeAllViews();addNav("Profiles","profiles");addNav("Mail Verification","mail");addNav("2FA","twofa");content.removeAllViews();if(tab.equals("profiles"))showProfiles();else if(tab.equals("mail"))showMail();else show2fa();}
    void panelStart(String title,String subtitle){
        LinearLayout p=column();p.setPadding(dp(17),dp(17),dp(17),dp(17));p.setBackground(bg(PANEL,LINE,18));content.addView(p,lp(-1,-2));
        TextView eyebrow=text("PROFILEFORGE  /  WORKSPACE",9,PURPLE,true);eyebrow.setLetterSpacing(0.13f);p.addView(eyebrow);gap(p,8);
        p.addView(text(title,20,TEXT,true));gap(p,6);p.addView(text(subtitle,12,MUTED,false));gap(p,16);
    }
    void showProfiles(){
        panelStart("Profile studio","Generate fictional sample profiles, usernames and custom-suffix passwords.");
        content.addView(text("01  /  NAME CATEGORY",10,MUTED,true));gap(content,5);
        genderSpinner=new Spinner(this);String[] genders={"All names","Male names","Female names"};genderSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,genders));content.addView(genderSpinner,lp(-1,45));gap(content,12);
        content.addView(text("02  /  BATCH SIZE",10,MUTED,true));gap(content,5);
        countSpinner=new Spinner(this);String[] counts={"1","10","20","50"};countSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,counts));countSpinner.setSelection(1);content.addView(countSpinner,lp(-1,45));gap(content,12);
        content.addView(text("03  /  PASSWORD SUFFIX",10,MUTED,true));gap(content,5);
        endingInput=edit("e.g. 29, 30, 31","29");content.addView(endingInput,lp(-1,48));gap(content,14);
        content.addView(button("Generate profiles  ↗",true,()->generateProfiles()),lp(-1,50));gap(content,8);
        content.addView(button("Export TXT",false,()->exportProfiles()),lp(-1,44));gap(content,18);
        resultList=column();content.addView(resultList,lp(-1,-2));generateProfiles();
    }
    String randomFrom(String[] a){return a[random.nextInt(a.length)];}
    String randomDigits(int n){StringBuilder s=new StringBuilder();for(int i=0;i<n;i++)s.append(random.nextInt(10));if(s.charAt(0)=='0')s.setCharAt(0,(char)('1'+random.nextInt(9)));return s.toString();}
    String makePassword(String ending){
        if(ending.isEmpty()||ending.length()>10)throw new IllegalArgumentException("Ending 1-10 characters ki honi chahiye.");
        if(ending.matches(".*[@#%&].*"))throw new IllegalArgumentException("@ # % & symbols allowed nahi hain.");
        StringBuilder s=new StringBuilder();for(int i=0;i<10-ending.length();i++)s.append(chars.charAt(random.nextInt(chars.length())));return s+ending;
    }
    String makeUsername(String first){
        String base=first.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]","");String candidate;
        do {String digits=randomDigits(5);candidate=base.substring(0,Math.min(base.length(),15-digits.length()))+digits;} while(usernames.contains(candidate));
        usernames.add(candidate);return candidate;
    }
    void generateProfiles(){
        try{
            String ending=endingInput.getText().toString().trim();makePassword(ending);
            int count=Integer.parseInt(countSpinner.getSelectedItem().toString());int g=genderSpinner.getSelectedItemPosition();
            profiles.clear();usernames.clear();resultList.removeAllViews();
            for(int i=0;i<count;i++){
                String first=g==1?randomFrom(maleNames):g==2?randomFrom(femaleNames):random.nextBoolean()?randomFrom(maleNames):randomFrom(femaleNames);
                Profile p=new Profile(first+" "+randomFrom(surnames),makeUsername(first),makePassword(ending));profiles.add(p);
                LinearLayout card=column();card.setPadding(dp(13),dp(13),dp(13),dp(13));card.setBackground(bg(Color.rgb(17,19,29),LINE,16));
                resultList.addView(card,lp(-1,-2));card.addView(text("PROFILE  /  SAMPLE",9,PURPLE,true));gap(card,5);card.addView(text(p.name,17,TEXT,true));gap(card,8);
                addCopyField(card,"USERNAME",p.username);addCopyField(card,"PASSWORD",p.password);gap(card,6);
                card.addView(button("Check on X ↗",false,()->openExternal("https://x.com/"+p.username)),lp(-1,42));gap(resultList,10);
            }
        }catch(Exception e){new AlertDialog.Builder(this).setMessage(e.getMessage()).setPositiveButton("OK",null).show();}
    }
    void addCopyField(LinearLayout parent,String label,String value){
        parent.addView(text(label,10,MUTED,true));LinearLayout r=row();parent.addView(r,lp(-1,42));
        TextView v=text(value,13,label.equals("PASSWORD")?GREEN:TEXT,false);r.addView(v,new LinearLayout.LayoutParams(0,dp(40),1));
        r.addView(button("Copy",false,()->copy(value)),lp(75,38));gap(parent,5);
    }
    void copy(String s){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("ProfileForge",s));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();}
    void exportProfiles(){
        if(profiles.isEmpty()){Toast.makeText(this,"Generate profiles first",Toast.LENGTH_SHORT).show();return;}
        StringBuilder s=new StringBuilder();for(int i=0;i<profiles.size();i++){Profile p=profiles.get(i);s.append("PROFILE ").append(i+1).append("\nName: ").append(p.name).append("\nUsername: ").append(p.username).append("\nPassword: ").append(p.password).append("\n\n");}
        try{
            if(Build.VERSION.SDK_INT>=29){
                ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,"profileforge-profiles.txt");v.put(MediaStore.Downloads.MIME_TYPE,"text/plain");
                android.net.Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
                try(java.io.OutputStream os=getContentResolver().openOutputStream(uri)){os.write(s.toString().getBytes("UTF-8"));}
            }else{
                File f=new File(getExternalFilesDir(null),"profileforge-profiles.txt");try(FileOutputStream os=new FileOutputStream(f)){os.write(s.toString().getBytes("UTF-8"));}
            }
            Toast.makeText(this,"TXT exported to Downloads",Toast.LENGTH_LONG).show();
        }catch(Exception e){Toast.makeText(this,"Export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    void showMail(){
        panelStart("Mail verification","Open your mailbox in the built-in browser. Site restrictions may apply.");
        content.addView(button("Open in Chrome ↗",true,()->openExternal(mailUrl)),lp(-1,44));gap(content,8);
        content.addView(button("Reload website",false,()->{if(mailWeb!=null)mailWeb.reload();}),lp(-1,42));gap(content,10);
        mailWeb=new WebView(this);mailWeb.setBackgroundColor(Color.WHITE);
        WebSettings settings=mailWeb.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setSupportMultipleWindows(false);settings.setLoadsImagesAutomatically(true);
        mailWeb.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){String u=r.getUrl().toString();if(u.startsWith("https://")||u.startsWith("http://"))return false;return true;}});
        mailWeb.setWebChromeClient(new WebChromeClient());
        content.addView(mailWeb,new LinearLayout.LayoutParams(-1,dp(520)));
        mailWeb.loadUrl(mailUrl);
        gap(content,10);content.addView(text("If this site blocks WebView or requires browser verification, use Open in Chrome. This app does not bypass website access controls.",11,MUTED,false));
    }
    void show2fa(){
        panelStart("Authenticator","Generate time-based one-time codes for accounts you own. Keep setup keys private.");
        content.addView(text("Base32 secret key",12,MUTED,true));gap(content,6);
        secretInput=edit("Paste setup key, e.g. JBSWY3DPEHPK3PXP","");secretInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);content.addView(secretInput,lp(-1,48));gap(content,12);
        content.addView(button("Generate / Refresh Code",true,()->updateOtp()),lp(-1,46));gap(content,15);
        otpStatus=text("Enter your setup key",12,MUTED,false);otpStatus.setGravity(Gravity.CENTER);content.addView(otpStatus,lp(-1,28));
        otpView=text("••••••",38,GREEN,true);otpView.setGravity(Gravity.CENTER);content.addView(otpView,lp(-1,70));
        otpTimer=text("30-second code window",12,MUTED,false);otpTimer.setGravity(Gravity.CENTER);content.addView(otpTimer,lp(-1,28));gap(content,8);
        content.addView(button("Copy code",false,()->copy(otpView.getText().toString())),lp(-1,42));gap(content,8);
        content.addView(text("Secret is held in memory only and is not sent to a server. TOTP codes refresh every 30 seconds.",11,MUTED,false));
        if(timer!=null)timer.stop();timer=new HandlerTimer();timer.runnable=new Runnable(){public void run(){if(currentTab.equals("twofa")&&!secretInput.getText().toString().trim().isEmpty())updateOtp();timer.handler.postDelayed(this,1000);}};timer.handler.postDelayed(timer.runnable,1000);
    }
    byte[] base32(String key) throws Exception{
        String alpha="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";String s=key.toUpperCase(Locale.ROOT).replaceAll("[\\\\s=\\\\-]","");
        if(s.isEmpty())throw new Exception("Enter a Base32 secret.");
        StringBuilder bits=new StringBuilder();for(char c:s.toCharArray()){int x=alpha.indexOf(c);if(x<0)throw new Exception("Invalid Base32 secret.");bits.append(String.format(Locale.ROOT,"%5s",Integer.toBinaryString(x)).replace(' ','0'));}
        byte[] out=new byte[bits.length()/8];for(int i=0;i<out.length;i++)out[i]=(byte)Integer.parseInt(bits.substring(i*8,i*8+8),2);
        if(out.length==0)throw new Exception("Secret is too short.");return out;
    }
    String totp(String secret) throws Exception{
        byte[] key=base32(secret);long counter=System.currentTimeMillis()/1000/30;
        byte[] msg=ByteBuffer.allocate(8).putLong(counter).array();
        Mac mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(key,"HmacSHA1"));byte[] hash=mac.doFinal(msg);
        int offset=hash[hash.length-1]&15;int binary=((hash[offset]&127)<<24)|((hash[offset+1]&255)<<16)|((hash[offset+2]&255)<<8)|(hash[offset+3]&255);
        return String.format(Locale.ROOT,"%06d",binary%1000000);
    }
    void updateOtp(){
        try{String s=secretInput.getText().toString().trim();if(s.isEmpty())throw new Exception("Enter a Base32 secret.");
            otpView.setText(totp(s));otpStatus.setText("Current authenticator code");otpTimer.setText("Refreshes in "+(30-(int)(System.currentTimeMillis()/1000%30))+" seconds");
        }catch(Exception e){otpView.setText("••••••");otpStatus.setText(e.getMessage());otpTimer.setText("30-second code window");}
    }
    void openExternal(String url){
        try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));startActivity(i);}
        catch(Exception e){Toast.makeText(this,"No browser app found",Toast.LENGTH_LONG).show();}
    }
    @Override public void onBackPressed(){
        if(currentTab.equals("mail")&&mailWeb!=null&&mailWeb.canGoBack()){mailWeb.goBack();return;}
        super.onBackPressed();
    }
    @Override protected void onDestroy(){if(timer!=null)timer.stop();if(mailWeb!=null)mailWeb.destroy();super.onDestroy();}
}
