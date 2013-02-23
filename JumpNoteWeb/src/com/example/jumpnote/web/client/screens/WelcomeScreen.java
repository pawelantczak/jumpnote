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

package com.example.jumpnote.web.client.screens;

import com.example.jumpnote.web.client.JumpNoteWeb;
import com.example.jumpnote.web.client.Screen;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * The welcome screen, containing a simple message indicating that the user needs to sign in
 * to see her notes.
 */
public class WelcomeScreen extends Screen {

    private static NoteEditorUiBinder uiBinder = GWT.create(NoteEditorUiBinder.class);

    @UiField
    Anchor signInLink;

    interface NoteEditorUiBinder extends UiBinder<Widget, WelcomeScreen> {
    }

    public WelcomeScreen() {
        initWidget(uiBinder.createAndBindUi(this));
        signInLink.setHref(JumpNoteWeb.sLoginUrl);
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        return this;
    }
}
