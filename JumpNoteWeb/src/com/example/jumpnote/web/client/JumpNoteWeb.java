/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jumpnote.web.client;

import com.example.jumpnote.allshared.JsonRpcClient;
import com.example.jumpnote.allshared.JsonRpcException;
import com.example.jumpnote.allshared.JumpNoteProtocol;
import com.example.jumpnote.allshared.JsonRpcClient.Call;
import com.example.jumpnote.web.client.screens.NoteEditor;
import com.example.jumpnote.web.client.screens.NotesList;
import com.example.jumpnote.web.client.screens.WelcomeScreen;
import com.example.jumpnote.web.jsonrpc.gwt.JsonRpcGwtClient;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The JumpNote web client entry point.
 */
public class JumpNoteWeb implements EntryPoint {
    private static final int TRANSIENT_MESSAGE_HIDE_DELAY = 5000;
    
    private final ScreenContainer mScreenContainer = new ScreenContainer();
    public static RootPanel sMessagePanel = null;

    public static String sLoginUrl = "";
    public static final JsonRpcGwtClient sJsonRpcClient = new JsonRpcGwtClient("/jumpnoterpc");
    public static Map<String, ModelJso.Note> sNotes = new HashMap<String, ModelJso.Note>();
    public static ModelJso.UserInfo sUserInfo = null;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        showMessage("Loading...", false);

        // Create login/logout links
        loadData(new Runnable() {
            public void run() {
                hideMessage();
                if (sUserInfo == null) {
                    RootPanel.get("screenPanel").add(new WelcomeScreen());
                } else {
                    mScreenContainer.addScreen("home", new NotesList());
                    mScreenContainer.addScreen("note", new NoteEditor());
                    mScreenContainer.setDefault("home");
                    mScreenContainer.install(RootPanel.get("screenPanel"));
                }
            }
        });
    }

    public void loadData(final Runnable callback) {
        List<Call> calls = new ArrayList<Call>();

        // Get and populate login information
        JSONObject userInfoParams = new JSONObject();
        userInfoParams.put(JumpNoteProtocol.UserInfo.ARG_LOGIN_CONTINUE,
                new JSONString(Window.Location.getHref()));

        final RootPanel loginPanel = RootPanel.get("loginPanel");
        calls.add(new Call(JumpNoteProtocol.UserInfo.METHOD, userInfoParams));
        calls.add(new Call(JumpNoteProtocol.NotesList.METHOD, null));

        sJsonRpcClient.callBatch(calls, new JsonRpcClient.BatchCallback() {
            public void onData(Object[] data) {
                // Process userInfo RPC call results
                JSONObject userInfoJson = (JSONObject) data[0];
                if (userInfoJson.containsKey(JumpNoteProtocol.UserInfo.RET_USER)) {
                    JumpNoteWeb.sUserInfo = (ModelJso.UserInfo) userInfoJson.get(
                            JumpNoteProtocol.UserInfo.RET_USER).isObject().getJavaScriptObject();
                    InlineLabel label = new InlineLabel();
                    label.getElement().setId("userNameLabel");
                    label.setText(sUserInfo.getEmail());
                    loginPanel.add(label);

                    loginPanel.add(new InlineLabel(" | "));

                    Anchor anchor = new Anchor("Sign out",
                            userInfoJson.get(JumpNoteProtocol.UserInfo.RET_LOGOUT_URL).isString()
                            .stringValue());
                    loginPanel.add(anchor);
                } else {
                    sLoginUrl = userInfoJson.get(JumpNoteProtocol.UserInfo.RET_LOGIN_URL).isString().stringValue();
                    Anchor anchor = new Anchor("Sign in", sLoginUrl);
                    loginPanel.add(anchor);
                }

                // Process notesList RPC call results
                JSONObject notesListJson = (JSONObject) data[1];
                if (notesListJson != null) {
                    JSONArray notesJson = notesListJson.get(JumpNoteProtocol.NotesList.RET_NOTES).isArray();
                    for (int i = 0; i < notesJson.size(); i++) {
                        ModelJso.Note note = (ModelJso.Note) notesJson.get(i).isObject()
                                .getJavaScriptObject();
                        sNotes.put(note.getId(), note);
                    }
                }

                callback.run();
            }

            public void onError(int callIndex, JsonRpcException caught) {
                // Don't show an error if the notes.list call failed with 403 forbidden, since
                // that's normal in the case of a user not yet logging in.
                if (callIndex == 1 && caught.getHttpCode() == 403)
                    return;

                showMessage("Error: " + caught.getMessage(), false);
            }
        });
    }

    public static void showMessage(String message, boolean isTransient) {
        if (sMessagePanel == null) {
            sMessagePanel = RootPanel.get("messagePanel");
        }

        sMessagePanel.setVisible(true);
        sMessagePanel.getElement().setInnerText(message);
        if (isTransient) {
            new Timer() {
                @Override
                public void run() {
                    sMessagePanel.setVisible(false);
                }
            }.schedule(TRANSIENT_MESSAGE_HIDE_DELAY);
        }
    }

    public static void hideMessage() {
        sMessagePanel.setVisible(false);
    }
}
