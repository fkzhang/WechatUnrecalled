package com.fkzhang.wechatunrecalled;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
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

        Switch disable_notification = (Switch) findViewById(R.id.disable_notification);
        disable_notification.setChecked(settingsHelper.getBoolean("disable_notification", false));
        disable_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setBoolean("disable_notification", isChecked);
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
