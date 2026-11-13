package com.hku.money;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.hku.money.data.LiveApi;

public class LoginActivity extends AppCompatActivity {
    private static final int TEAL = 0xff14636a;
    private static final int INK = 0xff192e29;
    private static final int MUTED = 0xff56665e;
    private static final int MINT = 0xfff2faf1;
    private boolean registering;
    private Runnable pendingNetworkAction;
    private final androidx.activity.result.ActivityResultLauncher<String> localNetworkPermission =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), granted -> {
                Runnable action = pendingNetworkAction;
                pendingNetworkAction = null;
                if (granted && action != null) action.run();
                else if (!granted) android.widget.Toast.makeText(this,
                        "Allow local network access to connect to this server.", android.widget.Toast.LENGTH_LONG).show();
            });

    private boolean requestLocalNetworkAccess(Runnable retry) {
        if (android.os.Build.VERSION.SDK_INT < 37 || checkSelfPermission(
                android.Manifest.permission.ACCESS_LOCAL_NETWORK) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        pendingNetworkAction = retry;
        localNetworkPermission.launch(android.Manifest.permission.ACCESS_LOCAL_NETWORK);
        return true;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        registering = state != null && state.getBoolean("registering");
        LiveApi api = new LiveApi(this);
        getWindow().setStatusBarColor(MINT);
        getWindow().setNavigationBarColor(MINT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(MINT);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(28), dp(36), dp(28), dp(24));
        page.setFocusableInTouchMode(true);
        scroll.addView(page);

        TextView monogram = label("b.", 30, TEAL, true);
        monogram.setGravity(Gravity.CENTER);
        GradientDrawable badge = new GradientDrawable();
        badge.setColor(0xffd5ed7b);
        badge.setCornerRadius(dp(18));
        monogram.setBackground(badge);
        page.addView(monogram, new LinearLayout.LayoutParams(dp(56), dp(56)));

        TextView brand = label("BUY OR BYE", 11, TEAL, true);
        brand.setLetterSpacing(.18f);
        add(page, brand, 20);
        add(page, label("A little pause.\nA better buy.", 34, INK, true), 12);
        add(page, label("Good decisions start here.", 15, MUTED, false), 10);

        TextView formTitle = label(registering ? "Create your account" : "Welcome back", 21, INK, true);
        add(page, formTitle, 32);
        TextInputLayout userBox = field(page, "Username", false);
        TextInputLayout passBox = field(page, "Password", true);
        TextInputEditText username = (TextInputEditText) userBox.getEditText();
        TextInputEditText password = (TextInputEditText) passBox.getEditText();
        username.setId(View.generateViewId());
        password.setId(View.generateViewId());
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            username.setAutofillHints(View.AUTOFILL_HINT_USERNAME);
            password.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
        }
        password.setImeOptions(EditorInfo.IME_ACTION_DONE);

        TextView status = label("", 13, 0xffa33e39, false);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.setVisibility(View.GONE);
        add(page, status, 8);

        MaterialButton submit = new MaterialButton(this);
        submit.setAllCaps(false);
        submit.setText(registering ? "Create account" : "Log in");
        submit.setTextSize(16);
        submit.setTypeface(null, Typeface.BOLD);
        submit.setCornerRadius(dp(16));
        submit.setInsetTop(0);
        submit.setInsetBottom(0);
        submit.setElevation(0);
        submit.setStateListAnimator(null);
        submit.setTextColor(Color.WHITE);
        submit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(TEAL));
        LinearLayout.LayoutParams submitLayout = new LinearLayout.LayoutParams(-1, dp(54));
        submitLayout.topMargin = dp(20);
        page.addView(submit, submitLayout);

        TextView mode = label(registering ? "Already a member? Log in" : "New here? Create an account", 14, TEAL, true);
        mode.setGravity(Gravity.CENTER);
        mode.setMinHeight(dp(48));
        add(page, mode, 6);
        mode.setOnClickListener(view -> {
            registering = !registering;
            formTitle.setText(registering ? "Create your account" : "Welcome back");
            submit.setText(registering ? "Create account" : "Log in");
            mode.setText(registering ? "Already a member? Log in" : "New here? Create an account");
            status.setVisibility(View.GONE);
            userBox.setError(null);
            passBox.setError(null);
        });

        TextView server = label("Server settings", 13, MUTED, false);
        server.setGravity(Gravity.CENTER);
        server.setMinHeight(dp(48));
        page.addView(server);
        server.setOnClickListener(view -> serverSettings(api));

        View spacer = new View(this);
        page.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1));
        TextView preview = label("Explore Preview  →", 14, TEAL, true);
        preview.setGravity(Gravity.CENTER);
        preview.setMinHeight(dp(48));
        add(page, preview, 16);
        TextView note = label("Local demo · no account needed", 12, MUTED, false);
        note.setGravity(Gravity.CENTER);
        page.addView(note);
        preview.setOnClickListener(view -> {
            api.startPreview();
            enterApp();
        });

        submit.setOnClickListener(view -> {
            userBox.setError(null);
            passBox.setError(null);
            String user = username.getText().toString().trim();
            String pass = password.getText().toString();
            if (user.isEmpty()) { userBox.setError("Enter your username"); username.requestFocus(); return; }
            if (pass.isEmpty()) { passBox.setError("Enter your password"); password.requestFocus(); return; }
            if (registering && !user.matches("[a-zA-Z0-9_]{3,64}")) {
                userBox.setError("Use 3–64 letters, numbers or underscores"); return;
            }
            if (registering && (pass.length() < 8 || pass.length() > 72)) {
                passBox.setError("Use 8–72 characters"); return;
            }
            if (requestLocalNetworkAccess(submit::performClick)) return;
            boolean createAccount = registering;
            submit.setEnabled(false);
            mode.setEnabled(false);
            preview.setEnabled(false);
            submit.setText(createAccount ? "Creating…" : "Logging in…");
            status.setVisibility(View.GONE);
            JsonObject payload = new JsonObject();
            payload.addProperty("username", user);
            payload.addProperty("password", pass);
            payload.addProperty("deviceId", api.deviceId());
            if (createAccount) payload.addProperty("displayName", user);
            api.request("POST", createAccount ? "auth/register" : "auth/login", payload, (data, error) -> {
                if (isFinishing() || isDestroyed()) return;
                submit.setEnabled(true);
                mode.setEnabled(true);
                preview.setEnabled(true);
                password.setText("");
                submit.setText(registering ? "Create account" : "Log in");
                if (error != null) {
                    status.setTextColor(0xffa33e39);
                    status.setText(error);
                    status.setVisibility(View.VISIBLE);
                    return;
                }
                if (createAccount) {
                    registering = false;
                    formTitle.setText("Welcome back");
                    submit.setText("Log in");
                    mode.setText("New here? Create an account");
                    status.setTextColor(TEAL);
                    status.setText("Account created. Log in to continue.");
                    status.setVisibility(View.VISIBLE);
                } else {
                    api.saveToken(data.getAsJsonObject().get("accessToken").getAsString());
                    enterApp();
                }
            });
        });
        password.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_DONE) return false;
            if (submit.isEnabled()) submit.performClick();
            return true;
        });
        setContentView(scroll);
        page.requestFocus();
    }

    private void serverSettings(LiveApi api) {
        com.google.android.material.bottomsheet.BottomSheetDialog sheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(20), dp(24), dp(24));
        panel.setBackgroundColor(Color.WHITE);
        panel.addView(label("Connect your backend", 22, INK, true));
        add(panel, label("Use your teammate’s IP or a deployed server.", 14, MUTED, false), 8);
        TextInputLayout box = field(panel, "Server address", false);
        TextInputEditText address = (TextInputEditText) box.getEditText();
        address.setText(api.baseUrl());
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        TextView status = label("Emulator on this computer: 10.0.2.2:8080", 12, MUTED, false);
        add(panel, status, 8);
        MaterialButton test = new MaterialButton(this);
        test.setText("Test connection");
        test.setAllCaps(false);
        test.setCornerRadius(dp(16));
        add(panel, test, 12);
        MaterialButton save = new MaterialButton(this);
        save.setText("Save address");
        save.setAllCaps(false);
        save.setCornerRadius(dp(16));
        panel.addView(save);
        TextView docs = label("Open API docs  ↗", 14, TEAL, true);
        docs.setGravity(Gravity.CENTER);
        docs.setMinHeight(dp(48));
        panel.addView(docs);
        test.setOnClickListener(view -> {
            if (requestLocalNetworkAccess(test::performClick)) return;
            test.setEnabled(false);
            status.setText("Checking API docs…");
            api.testConnection(address.getText().toString(), (data, error) -> {
                if (!sheet.isShowing() || isDestroyed()) return;
                test.setEnabled(true);
                status.setTextColor(error == null ? TEAL : 0xffa33e39);
                status.setText(error == null ? "Connected · " + data.getAsJsonObject().get("paths").getAsInt()
                        + " API paths · login and publishing found" : error);
            });
        });
        save.setOnClickListener(view -> {
            try {
                api.saveBaseUrl(address.getText().toString());
                sheet.dismiss();
            } catch (IllegalArgumentException error) {
                box.setError(error.getMessage());
            }
        });
        docs.setOnClickListener(view -> {
            try {
                String base = com.hku.money.data.ServerAddress.normalize(address.getText().toString());
                String root = base.substring(0, base.length() - "api/".length());
                startActivity(new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse(root + "swagger-ui/index.html")));
            } catch (IllegalArgumentException error) {
                box.setError(error.getMessage());
            } catch (android.content.ActivityNotFoundException error) {
                status.setText("No browser is available on this device.");
            }
        });
        sheet.setContentView(panel);
        sheet.show();
        View background = findViewById(android.R.id.content);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            background.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(
                    dp(10), dp(10), android.graphics.Shader.TileMode.CLAMP));
            sheet.setOnDismissListener(dialog -> background.setRenderEffect(null));
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("registering", registering);
        super.onSaveInstanceState(state);
    }

    private void enterApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setFontFeatureSettings("kern");
        text.setLineSpacing(dp(2), 1);
        if (bold) text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return text;
    }

    private void add(LinearLayout parent, View child, int topMargin) {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
        layout.topMargin = dp(topMargin);
        parent.addView(child, layout);
    }

    private TextInputLayout field(LinearLayout parent, String hint, boolean secret) {
        TextInputLayout box = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        box.setHint(hint);
        box.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        box.setBoxBackgroundColor(Color.WHITE);
        box.setBoxCornerRadii(dp(14), dp(14), dp(14), dp(14));
        box.setBoxStrokeColor(TEAL);
        box.setDefaultHintTextColor(android.content.res.ColorStateList.valueOf(MUTED));
        if (secret) box.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        TextInputEditText field = new TextInputEditText(box.getContext());
        field.setSingleLine(true);
        field.setTextSize(16);
        field.setTextColor(INK);
        field.setInputType(secret ? 129 : android.text.InputType.TYPE_CLASS_TEXT);
        field.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.addView(field, new LinearLayout.LayoutParams(-1, dp(58)));
        add(parent, box, 12);
        return box;
    }
}
