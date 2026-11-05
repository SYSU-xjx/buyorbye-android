package com.hku.money.ui;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.hku.money.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.math.BigDecimal;

/** Native reference screens. All business data is explicitly demo-only until API review. */
public class SectionFragment extends Fragment {
    private static final int TEAL=0xff14636a, INK=0xff192e29, BG=0xfff2faf1;
    private static final int LIME = 0xffd5ed7b;
    private static final int MUTED = 0xff56665e;
    private static final String[] CURRENCIES = {
        "CNY", "USD", "EUR", "GBP", "JPY", "KRW", "HKD", "TWD", "MOP",
        "SGD", "THB", "CAD", "AUD", "NZD", "CHF", "RUB", "MYR", "IDR"
    };
    private final java.util.ArrayList<android.net.Uri> photos = new java.util.ArrayList<>();
    private LinearLayout photoList;
    private final androidx.activity.result.ActivityResultLauncher<String> photoPicker =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents(), selected -> {
                for (android.net.Uri uri : selected) {
                    if (photos.size() == 4) break;
                    if (!photos.contains(uri)) photos.add(uri);
                }
                if (photoList != null) showPhotos();
            });

    @Override public void onDestroyView() {
        photoList = null;
        content = null;
        super.onDestroyView();
    }

    private java.util.Currency currency(String code) {
        return java.util.Currency.getInstance(code);
    }

    private void chooseCurrency(String selected, java.util.function.Consumer<String> onPick) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = column();
        panel.setPadding(dp(16), dp(16), dp(16), dp(20));
        heading(panel, "Display currency");
        panel.addView(text("Changes the display on Home only.", 14, MUTED));
        ScrollView scroll = new ScrollView(requireContext());
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, dp(290)));
        LinearLayout grid = column();
        scroll.addView(grid);
        for (int i = 0; i < CURRENCIES.length; i += 3) {
            LinearLayout row = new LinearLayout(requireContext());
            grid.addView(row);
            for (int j = i; j < i + 3; j++) {
                String code = CURRENCIES[j];
                LinearLayout cell = fieldColumn(row);
                TextView tile = text(code + "\n" + currency(code).getDisplayName(java.util.Locale.ENGLISH), 13, INK);
                tile.setGravity(android.view.Gravity.CENTER);
                tile.setPadding(dp(4), dp(8), dp(4), dp(8));
                tile.setMinHeight(dp(72));
                tile.setBackground(surface(code.equals(selected) ? LIME : BG));
                tile.setSelected(code.equals(selected));
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
                layout.topMargin = dp(8);
                cell.addView(tile, layout);
                tile.setOnClickListener(v -> { onPick.accept(code); sheet.dismiss(); });
            }
        }
        button(panel, "Close", sheet::dismiss);
        sheet.setContentView(panel);
        showModal(sheet);
    }
    private LinearLayout content;
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable surface(int color) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(22));
        d.setStroke(dp(1),0xffdce9dc); return d;
    }
    private TextView text(String value,int size,int color) {
        TextView t=new TextView(requireContext()); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0,dp(4),0,dp(4)); t.setLineSpacing(dp(2), 1f); return t;
    }
    private void heading(LinearLayout box,String value) {
        TextView t=text(value,20,INK); t.setTypeface(null,Typeface.BOLD); box.addView(t);
    }
    private LinearLayout column() {
        LinearLayout box=new LinearLayout(requireContext()); box.setOrientation(LinearLayout.VERTICAL); return box;
    }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout box=column(); box.setPadding(dp(18),dp(16),dp(18),dp(16)); box.setBackground(surface(Color.WHITE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(12),0,dp(4));
        parent.addView(box,lp); return box;
    }
    private void button(LinearLayout box,String label,Runnable action) {
        Button b=new Button(requireContext()); b.setText(label); b.setAllCaps(false); b.setElevation(0); b.setStateListAnimator(null); b.setTypeface(null, Typeface.BOLD); b.setMinHeight(dp(48)); b.setBackground(surface(TEAL)); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(TEAL));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.topMargin=dp(10); box.addView(b,lp);
        b.setOnClickListener(v->action.run());
    }
    private final java.util.List<android.app.Dialog> openModals = new java.util.ArrayList<>();

    private void showModal(android.app.Dialog dialog) {
        View activityBackground = requireActivity().findViewById(android.R.id.content);
        dialog.setOnDismissListener(ignored -> {
            openModals.remove(dialog);
            updateModalBlur(activityBackground);
        });
        dialog.show();
        openModals.add(dialog);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(openModals.size() > 1 ? .22f : .38f);
        }
        updateModalBlur(activityBackground);
    }

    private void updateModalBlur(View activityBackground) {
        if (android.os.Build.VERSION.SDK_INT < 31) return;
        float radius = dp(12);
        android.graphics.RenderEffect blur = android.graphics.RenderEffect.createBlurEffect(
                radius, radius, android.graphics.Shader.TileMode.CLAMP);
        activityBackground.setRenderEffect(openModals.isEmpty() ? null : blur);
        for (int i = 0; i < openModals.size(); i++) {
            android.view.Window window = openModals.get(i).getWindow();
            if (window != null) window.getDecorView().setRenderEffect(i == openModals.size() - 1 ? null : blur);
        }
    }

    private void pending() {
        showModal(new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Preview only")
                .setMessage("Nothing was sent.")
                .setPositiveButton("OK", null)
                .create());
    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state) {
        ScrollView scroll=new ScrollView(requireContext()); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        content=column(); content.setPadding(dp(20),dp(14),dp(20),dp(24)); scroll.addView(content);
        int id=NavHostFragment.findNavController(this).getCurrentDestination().getId();
        if(id==R.id.publishFragment) {
            LinearLayout screen = column();
            screen.setBackgroundColor(BG);
            screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
            LinearLayout footer = column();
            footer.setBackgroundColor(Color.WHITE);
            footer.setPadding(dp(20), dp(4), dp(20), dp(12));
            screen.addView(footer);
            publish(footer);
            return screen;
        } else if(id==R.id.channelsFragment) channels();
        else if(id==R.id.groupsFragment) groups(); else if(id==R.id.plazaFragment) plaza();
        else if(id==R.id.profileFragment) profile(); else if(id==R.id.profilePageFragment) profilePage(); else home();
        return scroll;
    }
    private void title(String value) {
        TextView eyebrow = text("BUY OR BYE   /   PREVIEW", 11, TEAL);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        content.addView(eyebrow);
        TextView title = text(value, 30, INK);
        title.setTypeface(null, Typeface.BOLD);
        content.addView(title);
    }
    private void navigate(int id) { NavHostFragment.findNavController(this).navigate(id); }
    private void addStats(LinearLayout card, String... stats) {
        LinearLayout row = new LinearLayout(requireContext());
        card.addView(row);
        for (String stat : stats) {
            int separator = stat.indexOf(' ');
            LinearLayout cell = column();
            row.addView(cell, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView number = text(stat.substring(0, separator), 22, TEAL);
            number.setGravity(android.view.Gravity.CENTER);
            number.setTypeface(null, Typeface.BOLD);
            cell.addView(number);
            TextView label = text(stat.substring(separator + 1), 13, INK);
            label.setGravity(android.view.Gravity.CENTER);
            cell.addView(label);
        }
    }
    private void home() {
        title("What’s on your wishlist?"); content.addView(text("Good buys start with a second opinion.", 15, MUTED));
        LinearLayout c = card(content);
        c.setBackground(surface(TEAL));
        android.content.SharedPreferences preferences = requireContext().getSharedPreferences("home_preferences", 0);
        String selected = preferences.getString("display_currency", "CNY");
        LinearLayout summary = new LinearLayout(requireContext());
        summary.setGravity(android.view.Gravity.CENTER_VERTICAL);
        c.addView(summary);
        TextView label = text("TOTAL SAVED", 12, LIME);
        summary.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        TextView picker = text("", 14, INK);
        picker.setPadding(dp(12), 0, dp(12), 0);
        picker.setMinHeight(dp(48));
        picker.setGravity(android.view.Gravity.CENTER);
        picker.setBackground(surface(LIME));
        summary.addView(picker);
        TextView savings = text("", 40, Color.WHITE);
        savings.setTypeface(null, Typeface.BOLD);
        c.addView(savings);
        java.util.function.Consumer<String> showCurrency = code -> {
            picker.setText(code + " ▾");
            picker.setContentDescription("Display currency: " + code);
            java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.ENGLISH);
            format.setCurrency(currency(code));
            int decimals = currency(code).getDefaultFractionDigits();
            format.setMinimumFractionDigits(decimals);
            format.setMaximumFractionDigits(decimals);
            savings.setText(format.format(0));
        };
        showCurrency.accept(selected);
        picker.setOnClickListener(v -> chooseCurrency(preferences.getString("display_currency", "CNY"), code -> {
            preferences.edit().putString("display_currency", code).apply();
            showCurrency.accept(code);
        }));
        int approved = 0;
        int rejected = 0;
        int decided = approved + rejected;
        long rejectionRate = decided == 0 ? 0 : Math.round(100.0 * rejected / decided);
        c.addView(text(approved + " approved  ·  " + rejected + " rejected", 14, Color.WHITE));
        c.addView(text("Rejection rate: " + rejectionRate + "%", 14, LIME));
        c=card(content); heading(c,"All caught up!"); c.addView(text("New requests will show up here.",14,0xff56665e));
        c=card(content); addStats(c, "0 Approved", "0 Passed", "0 Flowers");
        button(content,"Start a buy check",()->navigate(R.id.publishFragment));
        c=card(content); c.setBackground(surface(0xffd5ed7b)); c.addView(text("Need a second opinion? Share your group code with a friend.",16,TEAL));
    }
    private record ShoppingGroup(String name, String icon, String code, int members) {
        String description() {
            return name + " · " + members + (members == 1 ? " member" : " members");
        }
    }

    private java.util.List<ShoppingGroup> savedGroups() {
        java.util.List<ShoppingGroup> groups = new java.util.ArrayList<>();
        groups.add(new ShoppingGroup("Sample shopping circle", "💰", "941281", 3));
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("groups", 0);
        try {
            org.json.JSONArray rows = new org.json.JSONArray(prefs.getString("local_groups", "[]"));
            for (int i = 0; i < rows.length(); i++) {
                org.json.JSONObject row = rows.getJSONObject(i);
                groups.add(new ShoppingGroup(row.getString("name"), row.getString("icon"),
                        row.getString("code"), row.getInt("members")));
            }
        } catch (org.json.JSONException error) {
            android.util.Log.w("ShoppingGroups", "Could not read local groups", error);
        }
        return groups;
    }

    private void addGroup(ShoppingGroup group) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("groups", 0);
        try {
            org.json.JSONArray rows = new org.json.JSONArray(prefs.getString("local_groups", "[]"));
            rows.put(new org.json.JSONObject().put("name", group.name()).put("icon", group.icon())
                    .put("code", group.code()).put("members", group.members()));
            prefs.edit().putString("local_groups", rows.toString()).apply();
        } catch (org.json.JSONException error) {
            throw new IllegalStateException("Could not save group", error);
        }
    }

    private void groups() {
        title("My groups");
        content.addView(text("Better buying decisions, together.", 15, MUTED));
        LinearLayout actions = new LinearLayout(requireContext());
        content.addView(actions);
        button(fieldColumn(actions), "+ Create a group", this::createGroup);
        button(fieldColumn(actions), "Join with code", this::joinGroup);
        for (ShoppingGroup group : savedGroups()) {
            LinearLayout tile = card(content);
            heading(tile, group.icon() + "  " + group.name());
            tile.addView(text(group.members() + (group.members() == 1 ? " member" : " members")
                    + "  ·  Code " + group.code() + "  ›", 14, MUTED));
            tile.setOnClickListener(v -> groupDetails(group));
        }
        card(content).addView(text("Try joining with demo code 382615. Groups are saved on this device.", 14, MUTED));
    }

    private LinearLayout groupSheet(BottomSheetDialog sheet, String title) {
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout panel = column();
        panel.setPadding(dp(20), dp(8), dp(20), dp(16));
        panel.setBackground(surface(Color.WHITE));
        scroll.addView(panel);
        View handle = new View(requireContext());
        handle.setBackground(surface(0xffdce9dc));
        LinearLayout.LayoutParams handleLayout = new LinearLayout.LayoutParams(dp(36), dp(4));
        handleLayout.gravity = android.view.Gravity.CENTER;
        handleLayout.bottomMargin = dp(6);
        panel.addView(handle, handleLayout);
        LinearLayout top = new LinearLayout(requireContext());
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
        panel.addView(top);
        TextView heading = text(title, 23, INK);
        heading.setTypeface(null, Typeface.BOLD);
        top.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView close = text("×", 24, TEAL);
        close.setGravity(android.view.Gravity.CENTER);
        close.setContentDescription("Close");
        top.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        close.setOnClickListener(v -> sheet.dismiss());
        sheet.setContentView(scroll);
        sheet.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        return panel;
    }

    private void groupActions(LinearLayout panel, BottomSheetDialog sheet, String label, Runnable action) {
        LinearLayout row = new LinearLayout(requireContext());
        panel.addView(row);
        TextView cancel = text("Cancel", 15, TEAL);
        cancel.setGravity(android.view.Gravity.CENTER);
        cancel.setMinHeight(dp(48));
        cancel.setBackground(surface(BG));
        LinearLayout cancelCell = fieldColumn(row);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
        layout.topMargin = dp(10);
        cancelCell.addView(cancel, layout);
        cancel.setOnClickListener(v -> sheet.dismiss());
        button(fieldColumn(row), label, action);
    }

    private void createGroup() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Create a group");
        EditText name = input(panel, "Group name *", "e.g. Weekend shoppers", false);
        name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});
        RadioGroup icons = choices(panel, "Pick an icon", 0, "💰", "🏠", "💼", "🍓");
        RadioGroup moreIcons = choices(panel, "More icons", -1, "🏃", "📚", "☕", "🎯");
        icons.setOnCheckedChangeListener((group, id) -> {
            if (id != View.NO_ID) moreIcons.clearCheck();
        });
        moreIcons.setOnCheckedChangeListener((group, id) -> {
            if (id != View.NO_ID) icons.clearCheck();
        });
        EditText customIcon = input(panel, "Custom emoji", "Optional", false);
        customIcon.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(16)});
        groupActions(panel, sheet, "Create group", () -> {
            String value = name.getText().toString().trim();
            if (value.isEmpty()) {
                name.setError("Enter a group name.");
                name.requestFocus();
                return;
            }
            RadioGroup selectedRow = icons.getCheckedRadioButtonId() == View.NO_ID ? moreIcons : icons;
            RadioButton selected = selectedRow.findViewById(selectedRow.getCheckedRadioButtonId());
            String icon = customIcon.getText().toString().trim();
            if (icon.isEmpty()) icon = selected == null ? "💰" : selected.getText().toString();
            java.util.Set<String> existing = new java.util.HashSet<>();
            for (ShoppingGroup group : savedGroups()) existing.add(group.code());
            existing.add("382615");
            String code;
            do {
                code = Integer.toString(100000 + java.util.concurrent.ThreadLocalRandom.current().nextInt(900000));
            } while (existing.contains(code));
            ShoppingGroup group = new ShoppingGroup(value, icon, code, 1);
            addGroup(group);
            sheet.dismiss();
            content.removeAllViews();
            groups();
            groupDetails(group);
        });
        showModal(sheet);
    }

    private void joinGroup() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Join a group");
        panel.addView(text("Demo code: 382615", 14, MUTED));
        EditText code = input(panel, "6-digit group code *", "e.g. 382615", false);
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        code.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});
        groupActions(panel, sheet, "Join group", () -> {
            String value = code.getText().toString();
            if (!value.matches("[0-9]{6}")) {
                code.setError("Enter a 6-digit code.");
                return;
            }
            for (ShoppingGroup group : savedGroups()) {
                if (group.code().equals(value)) {
                    code.setError("You’re already in this group.");
                    return;
                }
            }
            if (!value.equals("382615")) {
                code.setError("Code not found in this preview. Try 382615.");
                return;
            }
            ShoppingGroup group = new ShoppingGroup("Weekend deal hunters", "🎯", value, 4);
            addGroup(group);
            sheet.dismiss();
            content.removeAllViews();
            groups();
            groupDetails(group);
        });
        showModal(sheet);
    }

    private void groupDetails(ShoppingGroup group) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, group.icon() + "  " + group.name());
        panel.addView(text(group.members() + (group.members() == 1 ? " member" : " members"), 15, MUTED));
        LinearLayout invite = card(panel);
        invite.setBackground(surface(LIME));
        invite.addView(text("GROUP CODE", 12, TEAL));
        heading(invite, group.code());
        LinearLayout actions = new LinearLayout(requireContext());
        panel.addView(actions);
        button(fieldColumn(actions), "Copy code", () -> {
            android.content.ClipboardManager clipboard = requireContext().getSystemService(android.content.ClipboardManager.class);
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Group code", group.code()));
            Toast.makeText(requireContext(), "Group code copied", Toast.LENGTH_SHORT).show();
        });
        button(fieldColumn(actions), "New buy check", () -> {
            sheet.dismiss();
            Bundle args = new Bundle();
            args.putString("group_code", group.code());
            NavHostFragment.findNavController(this).navigate(R.id.publishFragment, args);
        });
        showModal(sheet);
    }
    private record Proposal(String id, String title, String channel, String price,
                            String reason, String alternative, int buy, int waitVotes) {}

    private static final java.util.List<Proposal> SAMPLE_PROPOSALS = java.util.List.of(
            new Proposal("cake", "Caramel pudding cake", "食物", "11.80",
                    "I’m craving something sweet after class.", "Split one with a friend.", 2, 1),
            new Proposal("lamp", "A little desk lamp", "数码", "75",
                    "My desk feels gloomy when I study at night.", "Try moving the lamp I already own.", 4, 2),
            new Proposal("tote", "Canvas market tote", "生活", "28",
                    "I’d like a bag that fits groceries and my books.", "Use the backpack I already have.", 1, 3),
            new Proposal("tea", "Two mango teas", "食物", "22",
                    "There’s a two-for-one deal this afternoon.", "Make iced tea at home.", 5, 2));

    private android.content.SharedPreferences channelPreferences() {
        return requireContext().getSharedPreferences("channel_preferences", 0);
    }

    private android.content.SharedPreferences plazaVotes() {
        return requireContext().getSharedPreferences("plaza_votes", 0);
    }

    private boolean myChannels;
    private String activeChannel;

    private java.util.List<Proposal> visibleProposals() {
        android.content.SharedPreferences prefs = channelPreferences();
        java.util.List<Proposal> visible = new java.util.ArrayList<>();
        String muted = prefs.getString("muted", "");
        boolean filterPrice = myChannels || prefs.getBoolean("global_range", false);
        java.util.Set<String> blockedAccounts = profileSet("blocked");
        for (Proposal proposal : SAMPLE_PROPOSALS) {
            if (blockedAccounts.contains(author(proposal))) continue;
            if (myChannels && !prefs.getBoolean(proposal.channel(), false)) continue;
            if (myChannels && activeChannel != null && !activeChannel.equals(proposal.channel())) continue;
            if (filterPrice) {
                BigDecimal price = new BigDecimal(proposal.price());
                String low = prefs.getString("low", "");
                String high = prefs.getString("high", "");
                if (!low.isEmpty() && price.compareTo(new BigDecimal(low)) < 0) continue;
                if (!high.isEmpty() && price.compareTo(new BigDecimal(high)) > 0) continue;
            }
            String searchable = (proposal.title() + " " + proposal.reason() + " " + proposal.alternative()
                    + " " + channelLabel(proposal.channel())).toLowerCase(java.util.Locale.ROOT);
            boolean blocked = false;
            for (String word : muted.split("[,\n]")) {
                if (!word.isBlank() && searchable.contains(word.trim().toLowerCase(java.util.Locale.ROOT))) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) visible.add(proposal);
        }
        return visible;
    }

    private TextView plazaAction(String label, int fill, int foreground, Runnable action) {
        TextView view = text(label, 14, foreground);
        view.setGravity(android.view.Gravity.CENTER);
        view.setTypeface(null, Typeface.BOLD);
        view.setMinHeight(dp(48));
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setBackground(surface(fill));
        view.setOnClickListener(v -> action.run());
        return view;
    }

    private void plazaLabel(LinearLayout parent, String label) {
        TextView text = text(label, 11, TEAL);
        text.setLetterSpacing(.12f);
        text.setTypeface(null, Typeface.BOLD);
        parent.addView(text);
    }

    private void plaza() {
        plazaLabel(content, "BUY OR BYE  /  COMMUNITY");
        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        content.addView(header);
        TextView name = text("Plaza", 34, INK);
        name.setTypeface(null, Typeface.BOLD);
        header.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(plazaAction("Manage channels", Color.WHITE, TEAL, () -> navigate(R.id.channelsFragment)));
        content.addView(plazaAction("Cart Hall of Fame  →", BG, TEAL, this::ranks));
        RadioGroup tabs = choices(content, "", myChannels ? 1 : 0, "All posts", "My channels");
        for (int i = 0; i < tabs.getChildCount(); i++) tabs.getChildAt(i).setMinimumHeight(dp(48));
        tabs.setOnCheckedChangeListener((group, id) -> {
            myChannels = picked(group, 1);
            activeChannel = null;
            refreshPlaza();
        });
        if (myChannels) {
            HorizontalScrollView channelScroll = new HorizontalScrollView(requireContext());
            channelScroll.setHorizontalScrollBarEnabled(false);
            LinearLayout chips = new LinearLayout(requireContext());
            channelScroll.addView(chips);
            content.addView(channelScroll);
            addChannelChip(chips, "All followed", null);
            for (String key : channelOrder()) {
                if (channelPreferences().getBoolean(key, false)) addChannelChip(chips, channelLabel(key), key);
            }
        }
        java.util.List<Proposal> posts = visibleProposals();
        if (posts.isEmpty()) {
            LinearLayout empty = card(content);
            heading(empty, "Nothing to weigh in on yet");
            empty.addView(text("Follow a channel or widen your filters to find more buy checks.", 15, MUTED));
        } else {
            LinearLayout discovery = card(content);
            discovery.setBackground(surface(0xffe9f3cf));
            plazaLabel(discovery, "DISCOVER");
            heading(discovery, "Surprise me  →");
            discovery.addView(text("Pick a side on a random buy check.", 14, MUTED));
            discovery.setOnClickListener(v -> surprise(posts));
            for (Proposal proposal : posts) {
                LinearLayout tile = card(content);
                proposalSummary(tile, proposal);
                tile.addView(plazaAction("View details  →", Color.WHITE, TEAL, () -> proposalDetails(proposal)));
                proposalActions(tile, proposal, () -> {});
            }
        }
        content.addView(plazaAction("+ Start a buy check", TEAL, Color.WHITE, () -> navigate(R.id.publishFragment)));
    }

    private void refreshPlaza() {
        if (content != null) {
            content.removeAllViews();
            plaza();
        }
    }

    private void addChannelChip(LinearLayout row, String label, String key) {
        TextView chip = text(label, 14, INK);
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setMinHeight(dp(48));
        chip.setBackground(surface(java.util.Objects.equals(activeChannel, key) ? LIME : Color.WHITE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, dp(8), dp(8), 0);
        row.addView(chip, params);
        chip.setOnClickListener(v -> {
            activeChannel = key;
            refreshPlaza();
        });
    }

    private void proposalSummary(LinearLayout panel, Proposal proposal) {
        plazaLabel(panel, channelLabel(proposal.channel()).toUpperCase(java.util.Locale.ENGLISH) + "  /  " + author(proposal) + " · DEMO");
        heading(panel, proposal.title());
        TextView price = text("¥ " + proposal.price(), 28, TEAL);
        price.setTypeface(null, Typeface.BOLD);
        panel.addView(price);
        panel.addView(text(proposal.reason(), 15, INK));
        TextView alternative = text("Try instead  ·  " + proposal.alternative(), 13, MUTED);
        alternative.setPadding(dp(12), dp(8), dp(12), dp(8));
        alternative.setBackground(surface(BG));
        panel.addView(alternative);
        int votes = proposal.buy() + proposal.waitVotes()
                + (plazaVotes().contains(proposal.id() + "_choice") ? 1 : 0);
        panel.addView(text(votes + " votes cast  ·  Settles at the deadline", 13, TEAL));
    }

    private void proposalActions(LinearLayout panel, Proposal proposal, Runnable closeParent) {
        if (plazaVotes().contains(proposal.id() + "_choice")) {
            button(panel, "View results", () -> { closeParent.run(); results(proposal); });
            return;
        }
        LinearLayout row = new LinearLayout(requireContext());
        panel.addView(row);
        row.setPadding(0, dp(8), 0, 0);
        fieldColumn(row).addView(plazaAction("Buy it", TEAL, Color.WHITE, () -> voteReason(proposal, true, closeParent)));
        fieldColumn(row).addView(plazaAction("Wait", BG, TEAL, () -> voteReason(proposal, false, closeParent)));
    }

    private void proposalDetails(Proposal proposal) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Buy check");
        proposalSummary(panel, proposal);
        proposalActions(panel, proposal, sheet::dismiss);
        java.util.Set<String> watched = profileSet("watching");
        TextView watch = plazaAction(watched.contains(proposal.id()) ? "Stop watching" : "Watch this buy check",
                BG, TEAL, () -> {
                    if (!watched.remove(proposal.id())) watched.add(proposal.id());
                    saveProfileSet("watching", watched);
                    sheet.dismiss();
                    refreshCurrentProfile();
                });
        panel.addView(watch);
        panel.addView(plazaAction("Block " + author(proposal), BG, TEAL, () ->
                showModal(new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Block " + author(proposal) + "?")
                        .setMessage("Their posts will be hidden from Plaza.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Block", (dialog, which) -> {
                            java.util.Set<String> blocked = profileSet("blocked");
                            blocked.add(author(proposal));
                            saveProfileSet("blocked", blocked);
                            sheet.dismiss();
                            refreshCurrentProfile();
                        }).create())));
        showModal(sheet);
    }

    private void refreshCurrentProfile() {
        if (NavHostFragment.findNavController(this).getCurrentDestination().getId() == R.id.plazaFragment) {
            refreshPlaza();
        } else if (content != null) {
            content.removeAllViews();
            profilePage();
        }
    }

    private void surprise(java.util.List<Proposal> posts) {
        int[] index = {java.util.concurrent.ThreadLocalRandom.current().nextInt(posts.size())};
        LinearLayout panel = column();
        panel.setPadding(dp(20), dp(16), dp(20), dp(20));
        panel.setBackground(surface(Color.WHITE));
        Runnable[] render = new Runnable[1];
        androidx.appcompat.app.AppCompatDialog dialog = new androidx.appcompat.app.AppCompatDialog(requireContext()) {
            private float x;
            private float y;

            @Override public boolean dispatchTouchEvent(android.view.MotionEvent event) {
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
                    x = event.getX();
                    y = event.getY();
                } else if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
                    float dx = event.getX() - x;
                    if (posts.size() > 1 && Math.abs(dx) > dp(55) && Math.abs(dx) > 1.5f * Math.abs(event.getY() - y)) {
                        android.view.MotionEvent cancel = android.view.MotionEvent.obtain(event);
                        cancel.setAction(android.view.MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(cancel);
                        cancel.recycle();
                        index[0] = (index[0] + (dx < 0 ? 1 : posts.size() - 1)) % posts.size();
                        render[0].run();
                        return true;
                    }
                }
                return super.dispatchTouchEvent(event);
            }
        };
        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(panel);
        dialog.setContentView(scroll);
        render[0] = () -> {
            panel.removeAllViews();
            heading(panel, "Surprise me");
            panel.addView(text((index[0] + 1) + " of " + posts.size() + "  ·  Swipe to browse", 13, MUTED));
            Proposal proposal = posts.get(index[0]);
            proposalSummary(panel, proposal);
            proposalActions(panel, proposal, dialog::dismiss);
            if (posts.size() > 1) {
                LinearLayout arrows = new LinearLayout(requireContext());
                panel.addView(arrows);
                fieldColumn(arrows).addView(plazaAction("‹ Prev", BG, TEAL, () -> {
                    index[0] = (index[0] + posts.size() - 1) % posts.size();
                    render[0].run();
                }));
                fieldColumn(arrows).addView(plazaAction("Next ›", BG, TEAL, () -> {
                    index[0] = (index[0] + 1) % posts.size();
                    render[0].run();
                }));
            }
            panel.addView(plazaAction("Close", Color.WHITE, TEAL, dialog::dismiss));
            scroll.scrollTo(0, 0);
        };
        render[0].run();
        showModal(dialog);
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            window.setLayout(getResources().getDisplayMetrics().widthPixels - dp(36), -2);
        }
    }

    private void voteReason(Proposal proposal, boolean buy, Runnable closeParent) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        String choice = buy ? "Buy it" : "Wait";
        LinearLayout panel = groupSheet(sheet, buy ? "Why buy it?" : "Why wait?");
        EditText reason = input(panel, "Reason *", "Tell us why", true);
        limitedText(panel, reason);
        groupActions(panel, sheet, "Vote", () -> {
            String value = reason.getText().toString().trim();
            if (value.isEmpty()) {
                reason.setError("Add a reason to submit your vote.");
                reason.requestFocus();
                return;
            }
            awardVotePoint(proposal.id());
            plazaVotes().edit().putString(proposal.id() + "_choice", choice)
                    .putString(proposal.id() + "_reason", value).apply();
            sheet.dismiss();
            closeParent.run();
            refreshCurrentProfile();
            results(proposal);
        });
        showModal(sheet);
    }

    private void results(Proposal proposal) {
        String choice = plazaVotes().getString(proposal.id() + "_choice", "");
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Vote results");
        heading(panel, proposal.title());
        LinearLayout tally = card(panel);
        tally.setBackground(surface(LIME));
        addStats(tally, (proposal.buy() + ("Buy it".equals(choice) ? 1 : 0)) + " Buy it",
                (proposal.waitVotes() + ("Wait".equals(choice) ? 1 : 0)) + " Wait");
        panel.addView(text("Live tally · final at the deadline", 13, MUTED));
        LinearLayout response = card(panel);
        heading(response, "Your vote · " + choice);
        response.addView(text(plazaVotes().getString(proposal.id() + "_reason", ""), 15, INK));
        panel.addView(plazaAction("Other responses", BG, TEAL, () -> {
            BottomSheetDialog responses = new BottomSheetDialog(requireContext());
            LinearLayout body = groupSheet(responses, "Sample responses");
            body.addView(text("Buy it · If it fits your budget, go for it.", 14, INK));
            body.addView(text("Wait · Give it a day and see how you feel.", 14, INK));
            showModal(responses);
        }));
        showModal(sheet);
    }
    private android.content.SharedPreferences playPreferences() {
        return requireContext().getSharedPreferences("play_features", 0);
    }

    private String todayKey() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(new java.util.Date());
    }

    private void awardVotePoint(String proposalId) {
        android.content.SharedPreferences prefs = playPreferences();
        String key = "credited_" + proposalId;
        if (prefs.getBoolean(key, false)) return;
        String daily = "daily_" + todayKey();
        int count = prefs.getInt(daily, 0);
        android.content.SharedPreferences.Editor edit = prefs.edit().putBoolean(key, true);
        if (count < 5) {
            int previous = prefs.getInt("points", 0);
            edit.putInt("points", previous + 5).putInt(daily, count + 1);
            if (!judgeRank(previous).equals(judgeRank(previous + 5))) {
                Toast.makeText(requireContext(), "The court is in session! " + judgeRank(previous + 5), Toast.LENGTH_LONG).show();
            }
        }
        edit.apply();
    }

    private String judgeRank(int points) {
        if (points >= 100) return "Chief Justice";
        if (points >= 50) return "Senior Judge";
        if (points >= 15) return "Cart Judge";
        return "Juror";
    }

    private void judgeBadge() {
        int points = playPreferences().getInt("points", 0);
        int next = points < 15 ? 15 : points < 50 ? 50 : 100;
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Judge Badge");
        heading(panel, judgeRank(points) + "  ·  " + points + " pts");
        panel.addView(text(points >= 100 ? "Top rank unlocked."
                : (next - points) + " pts to " + judgeRank(next), 15, TEAL));
        panel.addView(text("+5 pts for your first reasoned vote on a buy check. Up to 25 pts per day.", 14, MUTED));
        panel.addView(text("Juror 0  ·  Cart Judge 15  ·  Senior Judge 50  ·  Chief Justice 100", 13, MUTED));
        showModal(sheet);
    }

    private record RankEntry(String name, BigDecimal amount, int daysAgo) {}

    private void ranks() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Cart Hall of Fame");
        panel.addView(text("Demo leaderboard  ·  CNY", 13, MUTED));
        RadioGroup period = choices(panel, "Period", 0, "Today", "Week", "Month");
        RadioGroup category = choices(panel, "Leaderboard", 0, "Wallet Wins", "Cart Confessions");
        LinearLayout list = column();
        panel.addView(list);
        Runnable render = () -> {
            list.removeAllViews();
            boolean saved = picked(category, 0);
            java.util.List<RankEntry> entries = new java.util.ArrayList<>(saved
                    ? java.util.List.of(new RankEntry("Mia", new BigDecimal("96"), 0),
                            new RankEntry("Sam", new BigDecimal("158"), 3),
                            new RankEntry("Jamie", new BigDecimal("275"), 13))
                    : java.util.List.of(new RankEntry("Alex", new BigDecimal("85"), 0),
                            new RankEntry("Jamie", new BigDecimal("129"), 2),
                            new RankEntry("Mia", new BigDecimal("220"), 14)));
            android.content.SharedPreferences prefs = playPreferences();
            if ((saved ? "skipped" : "bought").equals(prefs.getString("outcome", ""))) {
                long elapsed = Math.max(0, System.currentTimeMillis() - prefs.getLong("outcome_at", 0));
                int days = (int) java.util.concurrent.TimeUnit.MILLISECONDS.toDays(elapsed);
                entries.add(new RankEntry("You", new BigDecimal(prefs.getString("outcome_amount", "0")), days));
            }
            int limit = picked(period, 0) ? 0 : picked(period, 1) ? 6 : 29;
            entries.removeIf(entry -> entry.daysAgo() > limit);
            entries.sort((a, b) -> b.amount().compareTo(a.amount()));
            for (int i = 0; i < entries.size(); i++) {
                RankEntry entry = entries.get(i);
                LinearLayout row = card(list);
                if (i == 0) row.setBackground(surface(0xffe9f3cf));
                heading(row, (i + 1) + "   " + entry.name() + "   ·   ¥" + entry.amount().toPlainString());
                if (i == 0) row.addView(text(saved ? "Cart Escape Artist" : "Cart Confession of the Period", 13, TEAL));
            }
        };
        period.setOnCheckedChangeListener((group, id) -> render.run());
        category.setOnCheckedChangeListener((group, id) -> render.run());
        panel.addView(plazaAction("What happened next?", BG, TEAL, () -> purchaseOutcome(render)));
        render.run();
        showModal(sheet);
    }

    private void purchaseOutcome(Runnable refresh) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "What happened next?");
        panel.addView(text("Settled demo buy check  ·  Wireless headphones  ·  ¥120", 14, MUTED));
        RadioGroup outcome = choices(panel, "Your update", 2, "Bought it", "Skipped it", "Still thinking");
        EditText actual = input(panel, "Paid (CNY)", "e.g. 99", false);
        actual.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        actual.setVisibility(View.GONE);
        View label = panel.getChildAt(panel.getChildCount() - 2);
        label.setVisibility(View.GONE);
        outcome.setOnCheckedChangeListener((group, id) -> {
            int visibility = picked(group, 0) ? View.VISIBLE : View.GONE;
            actual.setVisibility(visibility);
            label.setVisibility(visibility);
        });
        button(panel, "Save update", () -> {
            String status = picked(outcome, 0) ? "bought" : picked(outcome, 1) ? "skipped" : "thinking";
            BigDecimal amount = new BigDecimal("120");
            if ("bought".equals(status)) {
                try {
                    amount = new BigDecimal(actual.getText().toString().trim());
                    if (amount.signum() <= 0 || amount.scale() > 2) throw new NumberFormatException();
                } catch (NumberFormatException error) {
                    actual.setError("Enter a positive price with up to 2 decimals.");
                    actual.requestFocus();
                    return;
                }
            }
            playPreferences().edit().putString("outcome", status)
                    .putString("outcome_amount", amount.toPlainString())
                    .putLong("outcome_at", System.currentTimeMillis()).apply();
            refresh.run();
            sheet.dismiss();
        });
        showModal(sheet);
    }

    private void decisionWheel() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        LinearLayout panel = groupSheet(sheet, "Wheel of Maybe");
        EditText item = input(panel, "What’s in your cart?", "Optional item name", false);
        item.setText(playPreferences().getString("tomorrow_item", ""));
        String[] outcomes = {"Buy it", "Skip it", "Sleep on it"};
        String[] advice = {"Check your budget, then go for it.", "Your wallet just breathed a sigh of relief.",
                "Leave it in the cart. See how you feel tomorrow."};
        View wheel = new View(requireContext()) {
            private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(android.graphics.Canvas canvas) {
                float size = Math.min(getWidth(), getHeight()) - dp(4);
                float x = getWidth() / 2f;
                float y = getHeight() / 2f;
                android.graphics.RectF circle = new android.graphics.RectF(x - size/2, y - size/2, x + size/2, y + size/2);
                int[] colors = {TEAL, LIME, 0xffd6ebdf};
                paint.setTextSize(dp(13));
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setTextAlign(android.graphics.Paint.Align.CENTER);
                for (int i = 0; i < outcomes.length; i++) {
                    int angle = -150 + i * 120;
                    paint.setColor(colors[i]);
                    canvas.drawArc(circle, angle, 120, true, paint);
                    double midpoint = Math.toRadians(angle + 60);
                    paint.setColor(i == 0 ? Color.WHITE : INK);
                    canvas.drawText(outcomes[i], x + (float) Math.cos(midpoint) * size * .28f,
                            y + (float) Math.sin(midpoint) * size * .28f + dp(4), paint);
                }
                paint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, dp(7), paint);
            }
        };
        LinearLayout.LayoutParams wheelLayout = new LinearLayout.LayoutParams(-1, dp(210));
        wheelLayout.topMargin = dp(8);
        panel.addView(wheel, wheelLayout);
        TextView result = text("", 15, INK);
        panel.addView(result);
        LinearLayout actions = column();
        TextView spin = plazaAction("Spin", TEAL, Color.WHITE, () -> {});
        panel.addView(spin);
        panel.addView(actions);
        spin.setOnClickListener(v -> {
            spin.setEnabled(false);
            actions.removeAllViews();
            result.setText("");
            int pick = java.util.concurrent.ThreadLocalRandom.current().nextInt(3);
            float target = (360 - pick * 120) % 360;
            float current = wheel.getRotation();
            float distance = (target - current % 360 + 360) % 360;
            wheel.animate().rotation(current + 1080 + distance).setDuration(1100)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        if (!sheet.isShowing()) return;
                        spin.setEnabled(true);
                        spin.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                        result.setText(outcomes[pick] + "  ·  " + advice[pick]);
                        if (pick == 2) actions.addView(plazaAction("Save for tomorrow", BG, TEAL, () -> {
                            String name = item.getText().toString().trim();
                            if (name.isEmpty()) { item.setError("Enter an item to save."); return; }
                            playPreferences().edit().putString("tomorrow_item", name).apply();
                            Toast.makeText(requireContext(), "Saved for tomorrow", Toast.LENGTH_SHORT).show();
                        }));
                        actions.addView(plazaAction("Ask the Plaza", BG, TEAL, () -> {
                            Bundle args = new Bundle();
                            args.putString("wheel_item", item.getText().toString().trim());
                            sheet.dismiss();
                            NavHostFragment.findNavController(this).navigate(R.id.publishFragment, args);
                        }));
                    }).start();
        });
        showModal(sheet);
    }

    private android.content.SharedPreferences profilePreferences() {
        return requireContext().getSharedPreferences("profile_activity", 0);
    }

    private java.util.Set<String> profileSet(String key) {
        return new java.util.LinkedHashSet<>(profilePreferences().getStringSet(key, java.util.Collections.emptySet()));
    }

    private void saveProfileSet(String key, java.util.Set<String> entries) {
        profilePreferences().edit().putStringSet(key, entries).apply();
    }

    private String author(Proposal proposal) {
        return switch (proposal.id()) {
            case "cake" -> "Mia";
            case "lamp" -> "Alex";
            case "tote" -> "Sam";
            default -> "Jamie";
        };
    }

    private void profile() {
        profileTitle("Profile", null);
        TextView avatar = text("TS", 26, Color.WHITE);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setGravity(android.view.Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(TEAL);
        avatar.setBackground(circle);
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(dp(72), dp(72));
        avatarLayout.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        avatarLayout.setMargins(0, dp(20), 0, dp(8));
        content.addView(avatar, avatarLayout);
        centeredProfileText(content, "Thoughtful Shopper", 23, INK, true);
        int points = playPreferences().getInt("points", 0);
        content.addView(plazaAction(judgeRank(points) + "  ·  " + points + " pts", 0xffe9f3cf, TEAL, this::judgeBadge));

        int votes = 0;
        for (Proposal proposal : SAMPLE_PROPOSALS) {
            if (plazaVotes().contains(proposal.id() + "_choice")) votes++;
        }
        LinearLayout metrics = new LinearLayout(requireContext());
        content.addView(metrics);
        profileMetric(fieldColumn(metrics), "MY BUY CHECKS", "0", "Submitted", Color.WHITE);
        profileMetric(fieldColumn(metrics), "PLAZA ACTIVITY", String.valueOf(votes), "Votes cast", 0xffe9f3cf);

        LinearLayout records = card(content);
        heading(records, "My records");
        LinearLayout shortcuts = new LinearLayout(requireContext());
        shortcuts.setPadding(0, dp(8), 0, 0);
        records.addView(shortcuts);
        profileTile(shortcuts, "↗", "Submitted", "submitted", 0xfff2ecd7);
        profileTile(shortcuts, "✓", "Voted", "voted", 0xffe2f0e9);
        profileTile(shortcuts, "☆", "Watching", "watching", 0xffe9f3cf);
        profileTile(shortcuts, "✎", "Drafts", "drafts", 0xffe7eef2);

        content.addView(plazaAction("Wheel of Maybe  →", Color.WHITE, TEAL, this::decisionWheel));

        LinearLayout privacy = card(content);
        heading(privacy, "Feed preferences");
        profilePreference(privacy, "Aa", "Muted words", "Hide posts by keyword", "muted", 0xfff2ecd7);
        profilePreference(privacy, "⊘", "Blocked accounts", "Manage hidden accounts", "blocked", 0xffe7eef2);
    }

    private void centeredProfileText(LinearLayout parent, String label, int size, int color, boolean bold) {
        TextView view = text(label, size, color);
        view.setGravity(android.view.Gravity.CENTER);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        parent.addView(view);
    }

    private void profileTitle(String label, String description) {
        TextView header = text(label, 30, INK);
        header.setTypeface(null, Typeface.BOLD);
        content.addView(header);
        if (description != null) content.addView(text(description, 14, MUTED));
    }

    private void profileMetric(LinearLayout parent, String label, String value, String caption, int fill) {
        LinearLayout metric = card(parent);
        metric.setPadding(dp(12), dp(12), dp(12), dp(12));
        metric.setBackground(surface(fill));
        plazaLabel(metric, label);
        TextView number = text(value, 27, TEAL);
        number.setTypeface(null, Typeface.BOLD);
        metric.addView(number);
        metric.addView(text(caption, 13, MUTED));
    }

    private TextView profileIcon(String glyph, int fill) {
        TextView icon = text(glyph, 21, TEAL);
        icon.setGravity(android.view.Gravity.CENTER);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setBackground(surface(fill));
        return icon;
    }

    private void profileTile(LinearLayout row, String glyph, String label, String page, int fill) {
        LinearLayout tile = fieldColumn(row);
        tile.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        TextView icon = profileIcon(glyph, fill);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
        centeredProfileText(tile, label, 12, INK, false);
        tile.setContentDescription(label);
        tile.setOnClickListener(v -> openProfilePage(page));
    }

    private void profilePreference(LinearLayout parent, String glyph, String label, String subtitle, String page, int fill) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        parent.addView(row);
        row.addView(profileIcon(glyph, fill), new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout labels = column();
        labels.setPadding(dp(12), 0, dp(8), 0);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView name = text(label, 15, INK);
        name.setTypeface(null, Typeface.BOLD);
        labels.addView(name);
        labels.addView(text(subtitle, 12, MUTED));
        row.addView(text("›", 22, TEAL));
        row.setContentDescription(label);
        row.setOnClickListener(v -> openProfilePage(page));
    }

    private void openProfilePage(String page) {
        Bundle args = new Bundle();
        args.putString("profile_page", page);
        NavHostFragment.findNavController(this).navigate(R.id.profilePageFragment, args);
    }

    private void profilePage() {
        TextView back = text("‹  Profile", 15, TEAL);
        back.setMinHeight(dp(48));
        back.setGravity(android.view.Gravity.CENTER_VERTICAL);
        content.addView(back);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        String page = requireArguments().getString("profile_page", "submitted");
        switch (page) {
            case "muted" -> mutedWordsPage();
            case "blocked" -> blockedPage();
            case "drafts" -> {
                profileTitle("Drafts", null);
                emptyProfileCard(content, "No drafts yet", "Saved drafts will appear here.");
            }
            default -> history(page);
        }
    }

    private void emptyProfileCard(LinearLayout parent, String title, String description) {
        LinearLayout empty = column();
        empty.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        empty.setPadding(dp(20), dp(36), dp(20), dp(28));
        parent.addView(empty);
        empty.addView(profileIcon("◇", 0xffe9f3cf), new LinearLayout.LayoutParams(dp(56), dp(56)));
        centeredProfileText(empty, title, 19, INK, true);
        centeredProfileText(empty, description, 14, MUTED, false);
    }

    private void history(String initialPage) {
        profileTitle("Buy check history", null);
        int initialTab = "voted".equals(initialPage) ? 1 : "watching".equals(initialPage) ? 2 : 0;
        LinearLayout searchPanel = card(content);
        EditText search = input(searchPanel, "Search records", "Item or channel", false);
        search.setBackground(surface(BG));
        RadioGroup tabs = choices(content, "", initialTab, "Submitted", "Voted", "Watching");
        LinearLayout records = column();
        content.addView(records);
        Runnable render = () -> {
            records.removeAllViews();
            String query = search.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            java.util.Set<String> watched = profileSet("watching");
            for (Proposal proposal : SAMPLE_PROPOSALS) {
                boolean matchesTab = picked(tabs, 1) && plazaVotes().contains(proposal.id() + "_choice")
                        || picked(tabs, 2) && watched.contains(proposal.id());
                String searchText = (proposal.title() + " " + channelLabel(proposal.channel()))
                        .toLowerCase(java.util.Locale.ROOT);
                if (!matchesTab || !searchText.contains(query)) continue;
                LinearLayout row = card(records);
                heading(row, proposal.title());
                row.addView(text(channelLabel(proposal.channel()) + "  ·  ¥" + proposal.price(), 14, MUTED));
                if (picked(tabs, 1)) {
                    row.addView(plazaAction("View results", BG, TEAL, () -> results(proposal)));
                } else {
                    row.addView(plazaAction("View buy check", BG, TEAL, () -> proposalDetails(proposal)));
                }
            }
            if (records.getChildCount() == 0) {
                String hint = !query.isEmpty() ? "Try another search."
                        : picked(tabs, 1) ? "Votes you cast in Plaza will appear here."
                        : picked(tabs, 2) ? "Watch a buy check from its details to save it here."
                        : "Your submitted buy checks will appear here once publishing is available.";
                emptyProfileCard(records, "No records yet", hint);
            }
        };
        tabs.setOnCheckedChangeListener((group, checked) -> render.run());
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { render.run(); }
            @Override public void afterTextChanged(android.text.Editable value) {}
        });
        render.run();
    }

    private void mutedWordsPage() {
        profileTitle("Muted words", "Hide matching posts in Plaza.");
        LinearLayout editor = card(content);
        fieldLabel(editor, "Add a keyword");
        LinearLayout entry = new LinearLayout(requireContext());
        entry.setGravity(android.view.Gravity.CENTER_VERTICAL);
        editor.addView(entry);
        EditText input = new EditText(requireContext());
        input.setSingleLine();
        input.setTextSize(15);
        input.setHint("e.g. spoilers");
        input.setHintTextColor(MUTED);
        input.setTextColor(INK);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(surface(BG));
        entry.addView(input, new LinearLayout.LayoutParams(0, dp(48), 1));
        java.util.Set<String> words = new java.util.LinkedHashSet<>();
        for (String word : channelPreferences().getString("muted", "").split("[,\\n]")) {
            if (!word.trim().isEmpty()) words.add(word.trim());
        }
        LinearLayout list = column();
        TextView add = plazaAction("Add", TEAL, Color.WHITE, () -> {
            String word = input.getText().toString().trim();
            if (word.isEmpty() || word.contains(",") || word.contains("\\n")) {
                input.setError("Enter one keyword.");
                return;
            }
            for (String existing : words) {
                if (existing.equalsIgnoreCase(word)) {
                    input.setError("That keyword is already muted.");
                    return;
                }
            }
            words.add(word);
            channelPreferences().edit().putString("muted", String.join(",", words)).apply();
            input.setText("");
            renderMutedList(list, words);
        });
        LinearLayout.LayoutParams addLayout = new LinearLayout.LayoutParams(-2, dp(48));
        addLayout.setMarginStart(dp(8));
        entry.addView(add, addLayout);
        content.addView(list);
        renderMutedList(list, words);
    }

    private void renderMutedList(LinearLayout list, java.util.Set<String> words) {
        list.removeAllViews();
        if (words.isEmpty()) emptyProfileCard(list, "No muted words yet", "Add a keyword to filter matching posts.");
        for (String word : words) {
            LinearLayout row = card(list);
            heading(row, word);
            row.addView(plazaAction("Remove", BG, TEAL, () -> {
                words.remove(word);
                channelPreferences().edit().putString("muted", String.join(",", words)).apply();
                renderMutedList(list, words);
            }));
        }
    }

    private void blockedPage() {
        profileTitle("Blocked accounts", "Hide someone’s posts from their buy check in Plaza.");
        LinearLayout list = column();
        content.addView(list);
        renderBlockedList(list);
    }

    private void renderBlockedList(LinearLayout list) {
        list.removeAllViews();
        java.util.Set<String> blocked = profileSet("blocked");
        if (blocked.isEmpty()) emptyProfileCard(list, "No blocked accounts", "Accounts you block will appear here.");
        for (String name : blocked) {
            LinearLayout row = card(list);
            heading(row, name);
            row.addView(plazaAction("Unblock", BG, TEAL, () -> {
                blocked.remove(name);
                saveProfileSet("blocked", blocked);
                renderBlockedList(list);
            }));
        }
    }
    private EditText input(String label, String hint, boolean multiline) {
        return input(content, label, hint, multiline);
    }
    private void fieldLabel(LinearLayout parent, String label) {
        TextView fieldLabel = text(label, 13, INK);
        fieldLabel.setTypeface(null, Typeface.BOLD);
        fieldLabel.setPadding(0, dp(12), 0, dp(6));
        parent.addView(fieldLabel);
    }
    private EditText input(LinearLayout parent, String label, String hint, boolean multiline) {
        fieldLabel(parent, label);
        EditText field = new EditText(requireContext());
        field.setTextSize(15);
        field.setHint(hint);
        field.setHintTextColor(MUTED);
        field.setTextColor(INK);
        field.setPadding(dp(12), dp(12), dp(12), dp(12));
        field.setBackground(surface(BG));
        field.setInputType(InputType.TYPE_CLASS_TEXT | (multiline ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        field.setMinHeight(dp(48));
        field.setMinLines(multiline ? 2 : 1);
        field.setGravity(multiline ? android.view.Gravity.TOP : android.view.Gravity.CENTER_VERTICAL);
        parent.addView(field, new LinearLayout.LayoutParams(-1, -2));
        return field;
    }
    private Spinner select(LinearLayout parent, String label, String... options) {
        fieldLabel(parent, label);
        LinearLayout frame = column();
        frame.setBackground(surface(BG));
        frame.setPadding(dp(4), 0, dp(8), 0);
        parent.addView(frame);
        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setContentDescription(label);
        spinner.setPadding(dp(8), 0, 0, 0);
        frame.addView(spinner, new LinearLayout.LayoutParams(-1, dp(48)));
        return spinner;
    }
    private LinearLayout fieldColumn(LinearLayout row) {
        LinearLayout field = column();
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(0, -2, 1);
        if (row.getChildCount() > 0) layout.setMarginStart(dp(10));
        row.addView(field, layout);
        return field;
    }
    private LinearLayout formSection(String step, String label) {
        LinearLayout section = card(content);
        TextView index = text(step, 11, TEAL);
        index.setLetterSpacing(0.1f);
        index.setTypeface(null, Typeface.BOLD);
        section.addView(index);
        heading(section, label);
        return section;
    }
    private RadioGroup choices(LinearLayout parent, String label, int selected,
                               String... options) {
        if (label.isEmpty()) {
            View space = new View(requireContext());
            parent.addView(space, new LinearLayout.LayoutParams(-1, dp(12)));
        } else {
            fieldLabel(parent, label);
        }
        RadioGroup group = new RadioGroup(requireContext());
        group.setOrientation(LinearLayout.HORIZONTAL);
        parent.addView(group);
        for (String option : options) {
            RadioButton tile = new RadioButton(requireContext());
            tile.setId(View.generateViewId());
            tile.setText(option);
            tile.setTextSize(options.length > 2 ? 13 : 14);
            tile.setTextColor(INK);
            tile.setTypeface(null, Typeface.BOLD);
            tile.setGravity(android.view.Gravity.CENTER);
            tile.setButtonDrawable((android.graphics.drawable.Drawable) null);
            tile.setPadding(dp(5), dp(8), dp(5), dp(8));
            tile.setMinHeight(dp(options.length > 2 ? 48 : 64));
            android.graphics.drawable.StateListDrawable colors = new android.graphics.drawable.StateListDrawable();
            colors.addState(new int[]{android.R.attr.state_checked}, surface(LIME));
            colors.addState(new int[]{}, surface(BG));
            tile.setBackground(colors);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, -2, 1);
            if (group.getChildCount() > 0) params.setMarginStart(dp(6));
            group.addView(tile, params);
        }
        if (selected >= 0) group.check(group.getChildAt(selected).getId());
        return group;
    }

    private boolean picked(RadioGroup choices, int index) {
        return choices.getCheckedRadioButtonId() == choices.getChildAt(index).getId();
    }

    private void limitedText(LinearLayout section, EditText field) {
        field.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(300)});
        TextView count = text("0/300", 12, MUTED);
        count.setGravity(android.view.Gravity.END);
        section.addView(count);
        field.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int length) {
                count.setText(s.length() + "/300");
            }
            @Override public void afterTextChanged(android.text.Editable value) {}
        });
    }

    private void showPhotos() {
        photoList.removeAllViews();
        for (android.net.Uri uri : photos) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            photoList.addView(row);
            ImageView preview = new ImageView(requireContext());
            preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            preview.setContentDescription("Selected photo " + (photos.indexOf(uri) + 1));
            // Decode a sized thumbnail rather than loading the full photo into memory.
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                try {
                    preview.setImageBitmap(requireContext().getContentResolver().loadThumbnail(
                            uri, new android.util.Size(dp(56), dp(56)), null));
                } catch (java.io.IOException ignored) {
                    preview.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                preview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            row.addView(preview, new LinearLayout.LayoutParams(dp(56), dp(56)));
            TextView remove = text("Remove photo " + (photos.indexOf(uri) + 1), 14, TEAL);
            remove.setMinHeight(dp(48));
            remove.setGravity(android.view.Gravity.CENTER);
            row.addView(remove, new LinearLayout.LayoutParams(0, -2, 1));
            remove.setOnClickListener(v -> { photos.remove(uri); showPhotos(); });
        }
        if (photos.size() < 4) {
            button(photoList, "+ Add photos (" + photos.size() + "/4)", () -> photoPicker.launch("image/*"));
        } else {
            photoList.addView(text("4/4 photos selected", 13, MUTED));
        }
    }

    private void publish(LinearLayout footer) {
        TextView back = text("←  Back", 15, TEAL);
        back.setMinHeight(dp(48));
        back.setGravity(android.view.Gravity.CENTER_VERTICAL);
        content.addView(back);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        title("Start a buy check");
        content.addView(text("Tempted? Ask your friends.", 15, MUTED));

        LinearLayout item = formSection("01  /  THE ITEM", "What caught your eye?");
        RadioGroup voteType = choices(item, "How should people vote?", 0, "Buy it / Wait", "Pick an option");
        LinearLayout options = column();
        item.addView(options);
        EditText firstOption = input(options, "Option 1 *", "e.g. Buy now", false);
        EditText secondOption = input(options, "Option 2 *", "e.g. Wait for a sale", false);
        options.setVisibility(View.GONE);
        voteType.setOnCheckedChangeListener((group, id) ->
                options.setVisibility(picked(group, 1) ? View.VISIBLE : View.GONE));
        EditText name = input(item, "Item name *", "e.g. Wireless headphones", false);
        if (getArguments() != null && getArguments().containsKey("wheel_item")) {
            name.setText(getArguments().getString("wheel_item"));
        }
        LinearLayout pricing = new LinearLayout(requireContext());
        item.addView(pricing);
        select(fieldColumn(pricing), "Currency", CURRENCIES);
        EditText amount = input(fieldColumn(pricing), "Price *", "0.00", false);
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText reason = input(item, "Why do you want it? *", "What makes it worth buying?", true);
        limitedText(item, reason);
        limitedText(item, input(item, "Cheaper alternatives", "Optional", true));
        fieldLabel(item, "Photos · Optional");
        item.addView(text("Choose up to 4 photos for this preview.", 13, MUTED));
        photoList = column();
        item.addView(photoList);
        showPhotos();

        LinearLayout audience = formSection("02  /  THE AUDIENCE", "Where should it go?");
        boolean fromWheel = getArguments() != null && getArguments().containsKey("wheel_item");
        RadioGroup destination = choices(audience, "Send to", fromWheel ? 1 : 0, "My group", "The Plaza");
        LinearLayout groupFields = column();
        audience.addView(groupFields);
        java.util.List<ShoppingGroup> groups = savedGroups();
        String[] groupOptions = new String[groups.size()];
        String preselected = getArguments() == null ? null : getArguments().getString("group_code");
        int selectedGroup = 0;
        for (int i = 0; i < groups.size(); i++) {
            ShoppingGroup group = groups.get(i);
            groupOptions[i] = group.icon() + "  " + group.description();
            if (group.code().equals(preselected)) selectedGroup = i;
        }
        select(groupFields, "Group", groupOptions).setSelection(selectedGroup);
        LinearLayout plazaFields = column();
        audience.addView(plazaFields);
        select(plazaFields, "Channel", "General", "Food", "Tech", "Lifestyle");
        plazaFields.setVisibility(fromWheel ? View.VISIBLE : View.GONE);
        groupFields.setVisibility(fromWheel ? View.GONE : View.VISIBLE);
        destination.setOnCheckedChangeListener((group, id) -> {
            groupFields.setVisibility(picked(group, 0) ? View.VISIBLE : View.GONE);
            plazaFields.setVisibility(picked(group, 1) ? View.VISIBLE : View.GONE);
        });
        input(audience, "Tags (optional)", "e.g. tech, everyday", false);

        LinearLayout voting = formSection("03  /  THE VOTE", "Set the ground rules");
        RadioGroup reviewers = choices(voting, "Who decides?", 0, "Multiple reviewers", "Single reviewer");
        choices(voting, "Voting ends in", 2, "1 hr", "2 hrs", "24 hrs", "7 days");
        LinearLayout threshold = column();
        voting.addView(threshold);
        EditText minimum = input(threshold, "Minimum votes *", "Default: 3", false);
        minimum.setText("3");
        minimum.setInputType(InputType.TYPE_CLASS_NUMBER);
        TextView singleHint = text("One vote is required at the deadline.", 13, MUTED);
        voting.addView(singleHint);
        singleHint.setVisibility(View.GONE);
        reviewers.setOnCheckedChangeListener((group, id) -> {
            threshold.setVisibility(picked(group, 0) ? View.VISIBLE : View.GONE);
            singleHint.setVisibility(picked(group, 1) ? View.VISIBLE : View.GONE);
        });
        voting.addView(text("Results are calculated after the deadline.", 13, MUTED));

        button(footer,"Review",()->{
            boolean valid=true;
            if(name.getText().toString().trim().isEmpty()){name.setError("Enter an item name.");valid=false;}
            if(reason.getText().toString().trim().isEmpty()){reason.setError("Please add a reason.");valid=false;}
            try {if(new BigDecimal(amount.getText().toString()).signum()<=0) throw new NumberFormatException();}catch(NumberFormatException ex){amount.setError("Enter a price above zero.");valid=false;}
            try {if(picked(reviewers, 0) && Integer.parseInt(minimum.getText().toString())<1)throw new NumberFormatException();}catch(NumberFormatException ex){minimum.setError("Enter a whole number above zero.");valid=false;}
            if (picked(voteType, 1)) {
                String first = firstOption.getText().toString().trim();
                String second = secondOption.getText().toString().trim();
                if (first.isEmpty()) { firstOption.setError("Enter the first option."); valid = false; }
                if (second.isEmpty() || first.equalsIgnoreCase(second)) {
                    secondOption.setError("Enter a different second option.");
                    valid = false;
                }
            }
            if (valid) pending();
            else for (EditText field : new EditText[]{firstOption, secondOption, name, amount, reason, minimum}) {
                if (field.isShown() && field.getError() != null) { field.requestFocus(); break; }
            }
        });
    }
    // Retain existing preference keys when localizing channel labels.
    private String channelLabel(String key) {
        return switch (key) {
            case "生活" -> "Lifestyle";
            case "食物" -> "Food";
            case "穿搭" -> "Fashion";
            case "美妆" -> "Beauty";
            case "数码" -> "Tech";
            case "追星" -> "Fandom";
            case "二次元" -> "Anime";
            case "游戏" -> "Gaming";
            case "艺术" -> "Art";
            case "旅行" -> "Travel";
            case "运动" -> "Sports";
            case "宠物" -> "Pets";
            case "学习" -> "Study";
            case "职场" -> "Work";
            case "情感" -> "Relationships";
            default -> key;
        };
    }
    private java.util.List<String> channelOrder() {
        String defaults = "生活,食物,穿搭,美妆,数码,追星,二次元,游戏,艺术,旅行,运动,宠物,学习,职场,情感";
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                channelPreferences().getString("order", defaults).split(",")));
    }

    private void channelTiles(LinearLayout grid, java.util.List<String> order, java.util.Set<String> followed) {
        grid.removeAllViews();
        LinearLayout row = null;
        for (int i = 0; i < order.size(); i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(requireContext());
                row.setPadding(0, dp(8), 0, 0);
                grid.addView(row);
            }
            String key = order.get(i);
            ToggleButton tile = new ToggleButton(requireContext());
            String label = channelLabel(key);
            tile.setTextOn(label);
            tile.setTextOff(label);
            tile.setText(label);
            tile.setAllCaps(false);
            tile.setTextSize(13);
            tile.setTextColor(INK);
            tile.setMinHeight(dp(48));
            tile.setTypeface(null, Typeface.BOLD);
            tile.setPadding(dp(2), 0, dp(2), 0);
            tile.setStateListAnimator(null);
            tile.setChecked(followed.contains(key));
            tile.setBackground(surface(tile.isChecked() ? LIME : BG));
            fieldColumn(row).addView(tile, new LinearLayout.LayoutParams(-1, -2));
            tile.setOnCheckedChangeListener((view, checked) -> {
                if (checked) followed.add(key);
                else followed.remove(key);
                tile.setBackground(surface(checked ? LIME : BG));
            });
            tile.setOnLongClickListener(v -> v.startDragAndDrop(
                    android.content.ClipData.newPlainText("Channel", key),
                    new View.DragShadowBuilder(v), key, 0));
            tile.setOnDragListener((v, event) -> {
                if (!(event.getLocalState() instanceof String dragged) || !order.contains(dragged)) return false;
                if (event.getAction() == android.view.DragEvent.ACTION_DROP && !dragged.equals(key)) {
                    int target = order.indexOf(key);
                    order.remove(dragged);
                    order.add(target, dragged);
                    grid.post(() -> channelTiles(grid, order, followed));
                }
                return true;
            });
        }
    }

    private void channels() {
        content.addView(plazaAction("‹  Back to Plaza", BG, TEAL,
                () -> NavHostFragment.findNavController(this).navigateUp()));
        plazaLabel(content, "PLAZA  /  PREFERENCES");
        TextView title = text("Manage channels", 30, INK);
        title.setTypeface(null, Typeface.BOLD);
        content.addView(title);

        android.content.SharedPreferences prefs = channelPreferences();
        java.util.List<String> order = channelOrder();
        java.util.Set<String> followed = new java.util.HashSet<>();
        for (String key : order) if (prefs.getBoolean(key, false)) followed.add(key);
        LinearLayout channelCard = card(content);
        heading(channelCard, "My channels");
        channelCard.addView(text("Tap to follow. Hold to reorder.", 13, MUTED));
        LinearLayout grid = column();
        channelCard.addView(grid);
        channelTiles(grid, order, followed);

        LinearLayout range = card(content);
        heading(range, "Price range");
        range.addView(text("CNY · Either limit can be blank", 13, MUTED));
        LinearLayout row = new LinearLayout(requireContext());
        range.addView(row);
        EditText low = input(fieldColumn(row), "Minimum", "e.g. 20", false);
        EditText high = input(fieldColumn(row), "Maximum", "e.g. 200", false);
        for (EditText field : new EditText[]{low, high}) {
            field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        low.setText(prefs.getString("low", ""));
        high.setText(prefs.getString("high", ""));
        CheckBox global = new CheckBox(requireContext());
        global.setText("Also apply to All posts");
        global.setChecked(prefs.getBoolean("global_range", false));
        range.addView(global);

        LinearLayout mutedPanel = card(content);
        EditText muted = input(mutedPanel, "Muted words", "Comma-separated keywords", false);
        muted.setText(prefs.getString("muted", ""));
        button(content, "Save", () -> {
            String min = low.getText().toString().trim();
            String max = high.getText().toString().trim();
            try {
                BigDecimal a = min.isEmpty() ? null : new BigDecimal(min);
                BigDecimal b = max.isEmpty() ? null : new BigDecimal(max);
                if ((a != null && a.signum() < 0) || (b != null && b.signum() < 0)
                        || (a != null && b != null && a.compareTo(b) > 0)) throw new NumberFormatException();
            } catch (NumberFormatException error) {
                low.setError("Enter a valid range. Min cannot exceed max.");
                low.requestFocus();
                return;
            }
            android.content.SharedPreferences.Editor edit = prefs.edit().putString("low", min)
                    .putString("high", max).putBoolean("global_range", global.isChecked())
                    .putString("muted", muted.getText().toString().trim())
                    .putString("order", String.join(",", order));
            for (String key : order) edit.putBoolean(key, followed.contains(key));
            edit.apply();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }
}
