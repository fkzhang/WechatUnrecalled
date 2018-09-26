package com.fkzhang.wechatunrecalled;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.fkzhang.wechatunrecalled.Util.SettingsHelper;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_TONE_PICKER_CUSTOM = 0;
    private static final int REQUEST_TONE_PICKER_NEW_COMMENT = 1;
    private static final int REQUEST_TONE_PICKER_MSG_RECALL = 2;
    private static final int REQUEST_TONE_PICKER_COMMENT_RECALL = 3;
    private MenuItem mMenuItemIcon;
    private SettingsHelper mSettingsHelper;
    private TextView ringtone_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSettingsHelper = new SettingsHelper(this, "com.fkzhang.wechatunrecalled");

        Switch show_content = (Switch) findViewById(R.id.show_content);
        show_content.setChecked(mSettingsHelper.getBoolean("show_content", false));
        show_content.setOnCheckedChangeListener(this);

        Switch prevent_moments_recall = (Switch) findViewById(R.id.enable_moment_recall_prevention);
        prevent_moments_recall.setChecked(mSettingsHelper.getBoolean("prevent_moments_recall", true));
        prevent_moments_recall.setOnCheckedChangeListener(this);

        Switch prevent_comments_recall = (Switch) findViewById(R.id.enable_comment_recall_prevention);
        prevent_comments_recall.setChecked(mSettingsHelper.getBoolean("prevent_comments_recall", true));
        prevent_comments_recall.setOnCheckedChangeListener(this);

        Switch snslucky = (Switch) findViewById(R.id.snslucky);
        snslucky.setChecked(mSettingsHelper.getBoolean("snslucky", true));
        snslucky.setOnCheckedChangeListener(this);

        findViewById(R.id.replace_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomNotificationDialog("custom", R.string.replace_wechat_notification);
            }
        });
        findViewById(R.id.customize_new_comment_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomNotificationDialog("new_comment", R.string.customize_new_comment_notification);
            }
        });
        findViewById(R.id.customize_msg_recall_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomNotificationDialog("msg_recall", R.string.customize_recall_notification);
            }
        });
        findViewById(R.id.customize_comment_recall_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomNotificationDialog("comment_recall", R.string.customize_comment_recall_notification);
            }
        });

        EditText recalled_message = (EditText) findViewById(R.id.editText);
        if (TextUtils.isEmpty(mSettingsHelper.getString("recalled", null))) {
            mSettingsHelper.setString("recalled", getString(R.string.recalled_msg_content));
        }
        recalled_message.setText(mSettingsHelper.getString("recalled", "(Prevented)"));
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

                mSettingsHelper.setString("recalled", s.toString());
            }
        });

        EditText comment_recalled_message = (EditText) findViewById(R.id.editText2);
        if (TextUtils.isEmpty(mSettingsHelper.getString("comment_recall_content", null))) {
            mSettingsHelper.setString("comment_recall_content",
                    getString(R.string.comment_recall_content));
        }
        comment_recalled_message.setText(mSettingsHelper.getString("comment_recall_content",
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

                mSettingsHelper.setString("comment_recall_content", s.toString());
            }
        });

        final Activity thisActivity = this;
        TextView support = (TextView) findViewById(R.id.textView0);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, SupportActivity.class);
                startActivity(intent);
            }
        });


        mSettingsHelper.setString("img_summary", getString(R.string.recalled_img_summary));
        mSettingsHelper.setString("video_summary", getString(R.string.recalled_video_summary));
        mSettingsHelper.setString("new_comment", getString(R.string.new_comment));
        mSettingsHelper.setString("reply", getString(R.string.reply));
        mSettingsHelper.setString("audio", getString(R.string.audio));
        mSettingsHelper.setString("emoji", getString(R.string.emoji));
        mSettingsHelper.setString("share_image", getString(R.string.share_image));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringTone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            ringtone_name.setText(ringTone.getTitle(this));
            switch (requestCode) {
                case REQUEST_TONE_PICKER_CUSTOM:
                    mSettingsHelper.setString("custom_ringtone", uri.toString());
                    break;
                case REQUEST_TONE_PICKER_NEW_COMMENT:
                    mSettingsHelper.setString("new_comment_ringtone", uri.toString());
                    break;
                case REQUEST_TONE_PICKER_MSG_RECALL:
                    mSettingsHelper.setString("msg_recall_ringtone", uri.toString());
                    break;
                case REQUEST_TONE_PICKER_COMMENT_RECALL:
                    mSettingsHelper.setString("comment_recall_ringtone", uri.toString());
                    break;
            }
        }
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.show_content:
                mSettingsHelper.setBoolean("show_content", isChecked);
                break;
            case R.id.enable_moment_recall_prevention:
                mSettingsHelper.setBoolean("prevent_moments_recall", isChecked);
                break;
            case R.id.enable_comment_recall_prevention:
                mSettingsHelper.setBoolean("prevent_comments_recall", isChecked);
                break;
            case R.id.snslucky:
                mSettingsHelper.setBoolean("snslucky", isChecked);
                break;
        }
    }

    private void requestRingtone(String tag) {
        if (!mSettingsHelper.getBoolean(tag + "_ringtone_enable", false))
            return;

        int request_code = -1;
        switch (tag) {
            case "custom":
                request_code = REQUEST_TONE_PICKER_CUSTOM;
                break;
            case "new_comment":
                request_code = REQUEST_TONE_PICKER_NEW_COMMENT;
                break;
            case "msg_recall":
                request_code = REQUEST_TONE_PICKER_MSG_RECALL;
                break;
            case "comment_recall":
                request_code = REQUEST_TONE_PICKER_COMMENT_RECALL;
        }

        if (request_code == -1)
            return;

        String uri = mSettingsHelper.getString(tag + "_ringtone", "");
        final Uri currentTone = TextUtils.isEmpty(uri) ? RingtoneManager
                .getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION) :
                Uri.parse(uri);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone_selection));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, request_code);
    }

    private void showCustomNotificationDialog(final String tag, int title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = this.getLayoutInflater().inflate(R.layout.dialog_customize_notification, null);

        final TextView ringtone_select = (TextView) view.findViewById(R.id.ringtone_select);
        ringtone_name = (TextView) view.findViewById(R.id.ringtone_name);
        ringtone_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRingtone(tag);
            }
        });
        ringtone_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRingtone(tag);
            }
        });

        String uri = mSettingsHelper.getString(tag + "_ringtone", "");
        if (TextUtils.isEmpty(uri)) {
            ringtone_name.setText(R.string.default_text);
        } else {
            Ringtone ringTone = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(uri));
            ringtone_name.setText(ringTone.getTitle(this));
        }

        Switch notification_enable = (Switch) view.findViewById(R.id.enable_notification);
        notification_enable.setChecked(mSettingsHelper.getBoolean(tag + "_notification_enable", true));
        notification_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingsHelper.setBoolean(tag + "_notification_enable", isChecked);
            }
        });

        Switch ringtone_switch = (Switch) view.findViewById(R.id.ringtone);
        ringtone_switch.setChecked(mSettingsHelper.getBoolean(tag + "_ringtone_enable", false));
        ringtone_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingsHelper.setBoolean(tag + "_ringtone_enable", isChecked);
                if (isChecked) {
                    ringtone_select.setVisibility(View.VISIBLE);
                    ringtone_name.setVisibility(View.VISIBLE);
                } else {
                    ringtone_select.setVisibility(View.INVISIBLE);
                    ringtone_name.setVisibility(View.INVISIBLE);
                }
            }
        });

        Switch vibrate_switch = (Switch) view.findViewById(R.id.vibrate);
        vibrate_switch.setChecked(mSettingsHelper.getBoolean(tag + "_vibrate_enable", false));
        vibrate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettingsHelper.setBoolean(tag + "_vibrate_enable", isChecked);
            }
        });

        if (!mSettingsHelper.getBoolean(tag + "_ringtone_enable", false)) {
            ringtone_select.setVisibility(View.INVISIBLE);
            ringtone_name.setVisibility(View.INVISIBLE);
        }

        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
}
