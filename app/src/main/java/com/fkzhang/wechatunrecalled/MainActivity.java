package com.fkzhang.wechatunrecalled;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private MenuItem mMenuItemIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SettingsHelper settingsHelper = new SettingsHelper(this, "com.fkzhang.wechatunrecalled");

        Switch enable_recall_notification = (Switch) findViewById(R.id.enable_recall_notification);
        enable_recall_notification.setChecked(settingsHelper.getBoolean("enable_recall_notification", true));
        enable_recall_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setBoolean("enable_recall_notification", isChecked);
            }
        });

        Switch enable_comment_recall_notification = (Switch) findViewById(R.id.enable_comment_recall_notification);
        enable_comment_recall_notification.setChecked(settingsHelper.getBoolean("enable_comment_recall_notification", true));
        enable_comment_recall_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setBoolean("enable_comment_recall_notification", isChecked);
            }
        });
        Switch enable_new_comment_notification = (Switch) findViewById(R.id.enable_new_comment_notification);
        enable_new_comment_notification.setChecked(settingsHelper.getBoolean("enable_new_comment_notification", false));
        enable_new_comment_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setBoolean("enable_new_comment_notification", isChecked);
            }
        });

        Switch show_content = (Switch) findViewById(R.id.show_content);
        show_content.setChecked(settingsHelper.getBoolean("show_content", false));
        show_content.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setBoolean("show_content", isChecked);
            }
        });

        EditText recalled_message = (EditText) findViewById(R.id.editText);
        if (TextUtils.isEmpty(settingsHelper.getString("recalled", null))) {
            settingsHelper.setString("recalled", getString(R.string.recalled_msg_content));
        }
        recalled_message.setText(settingsHelper.getString("recalled", "(Prevented)"));
        recalled_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String t = s.toString().trim();
                if (TextUtils.isEmpty(t))
                    return;

                settingsHelper.setString("recalled", s.toString());
            }
        });

        EditText comment_recalled_message = (EditText) findViewById(R.id.editText2);
        if (TextUtils.isEmpty(settingsHelper.getString("comment_recall_content", null))) {
            settingsHelper.setString("comment_recall_content",
                    getString(R.string.comment_recall_content));
        }
        comment_recalled_message.setText(settingsHelper.getString("comment_recall_content",
                getString(R.string.comment_recall_content)));
        comment_recalled_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String t = s.toString().trim();
                if (TextUtils.isEmpty(t))
                    return;

                settingsHelper.setString("comment_recall_content", s.toString());
            }
        });

        settingsHelper.setString("recalled_img_summary", getString(R.string.recalled_img_summary));
        settingsHelper.setString("recalled_video_summary", getString(R.string.recalled_video_summary));
        settingsHelper.setString("new_comment", getString(R.string.new_comment));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuItemIcon = menu.findItem(R.id.action_icon);
        setMenuIconTitle();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_icon:
                toggleLauncherIcon(!isIconEnabled());
                setMenuIconTitle();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMenuIconTitle() {
        if (isIconEnabled()) {
            mMenuItemIcon.setTitle(R.string.hide_icon);
        } else {
            mMenuItemIcon.setTitle(R.string.show_icon);
        }
    }

    private void toggleLauncherIcon(boolean newValue) {
        PackageManager packageManager = this.getPackageManager();
        int state = newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(getIconComponentName(), state, PackageManager.DONT_KILL_APP);
    }

    private ComponentName getIconComponentName() {
        return new ComponentName(this, "com.fkzhang.wechatunrecalled.MainActivity-Alias");
    }

    private boolean isIconEnabled() {
        return this.getPackageManager().getComponentEnabledSetting(getIconComponentName()) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }
}
