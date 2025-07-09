package com.zoo.automator;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;

public class MyAccessibilityService extends AccessibilityService {
    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final String TARGET_URL = "https://www.example.com";
    private boolean hasInputUrl = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Only act when Chrome is in the foreground
        if (event.getPackageName() != null && CHROME_PACKAGE.equals(event.getPackageName().toString())) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) return;
            // Step 1: Find and click the address bar (URL bar)
            AccessibilityNodeInfo urlBar = findNodeByViewIdOrDesc(rootNode, "com.android.chrome:id/url_bar", "Search or type web address");
            if (urlBar != null && urlBar.isClickable()) {
                urlBar.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                // Step 2: Input the URL (using clipboard for reliability)
                if (!hasInputUrl) {
                    setClipboardText(TARGET_URL);
                    urlBar.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    urlBar.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    hasInputUrl = true;
                    // Step 3: Try to find and click the "Go" or "Enter" button on the keyboard after a short delay
                    new Handler().postDelayed(() -> {
                        AccessibilityNodeInfo root = getRootInActiveWindow();
                        if (root != null) {
                            AccessibilityNodeInfo goButton = findNodeByText(root, "Go");
                            if (goButton == null) {
                                goButton = findNodeByText(root, "Enter");
                            }
                            if (goButton != null && goButton.isClickable()) {
                                goButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }, 500);
                }
            }
        } else {
            hasInputUrl = false; // Reset when leaving Chrome
        }
    }

    private AccessibilityNodeInfo findNodeByViewIdOrDesc(AccessibilityNodeInfo root, String viewId, String desc) {
        if (root == null) return null;
        // Try by viewId
        if (root.getViewIdResourceName() != null && root.getViewIdResourceName().equals(viewId)) {
            return root;
        }
        // Try by content description or text
        CharSequence contentDesc = root.getContentDescription();
        CharSequence text = root.getText();
        if ((contentDesc != null && contentDesc.toString().contains(desc)) ||
            (text != null && text.toString().contains(desc))) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByViewIdOrDesc(root.getChild(i), viewId, desc);
            if (result != null) return result;
        }
        return null;
    }

    // Helper to find a node by visible text
    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo root, String text) {
        if (root == null) return null;
        CharSequence nodeText = root.getText();
        if (nodeText != null && nodeText.toString().equalsIgnoreCase(text)) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNodeByText(root.getChild(i), text);
            if (result != null) return result;
        }
        return null;
    }

    private void setClipboardText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onInterrupt() {}
}
