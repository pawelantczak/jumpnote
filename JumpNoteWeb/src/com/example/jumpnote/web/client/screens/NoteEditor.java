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

import com.example.jumpnote.allshared.JsonRpcClient;
import com.example.jumpnote.allshared.JsonRpcException;
import com.example.jumpnote.allshared.JumpNoteProtocol;
import com.example.jumpnote.web.client.JumpNoteWeb;
import com.example.jumpnote.web.client.ModelJso;
import com.example.jumpnote.web.client.Screen;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * The create/edit note screen.
 */
public class NoteEditor extends Screen {

    private static NoteEditorUiBinder uiBinder = GWT.create(NoteEditorUiBinder.class);

    interface NoteEditorUiBinder extends UiBinder<Widget, NoteEditor> {
    }

    private String mEditNoteId;

    @UiField
    HeadingElement heading;

    @UiField
    TextBox noteTitle;

    @UiField
    TextArea noteBody;

    @UiField
    PushButton saveButton;

    @UiField
    PushButton revertButton;

    public NoteEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        heading.setInnerText("New Note");
    }

    public NoteEditor(String editNoteId) {
        this.mEditNoteId = editNoteId;
        initWidget(uiBinder.createAndBindUi(this));
        heading.setInnerText("Editing note: " + editNoteId);
        noteTitle.setText(JumpNoteWeb.sNotes.get(mEditNoteId).getTitle());
        noteBody.setText(JumpNoteWeb.sNotes.get(mEditNoteId).getBody());
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        if (args.size() == 0) {
            if (mEditNoteId == null) {
                this.noteTitle.setText("");
                this.noteBody.setText("");
                return this;
            } else
                return new NoteEditor();
        } else if (args.size() == 1) {
            if (args.get(0).equals(mEditNoteId))
                return this;
            else
                return new NoteEditor(args.get(0));
        }

        return null;
    }

    @Override
    public void onShow() {
        JumpNoteWeb.hideMessage();
        noteTitle.setFocus(true);
        super.onShow();
    }

    @UiHandler("saveButton")
    void onSaveClick(ClickEvent e) {
        String method;

        ModelJso.Note note = ModelJso.Note.create(noteTitle.getText(), noteBody.getText());

        if (mEditNoteId == null) {
            method = JumpNoteProtocol.NotesCreate.METHOD;
        } else {
            method = JumpNoteProtocol.NotesEdit.METHOD;
            note.setId(mEditNoteId);
        }

        JSONObject paramsJson = new JSONObject();
        paramsJson.put(JumpNoteProtocol.NotesEdit.ARG_NOTE, new JSONObject(note));

        JumpNoteWeb.showMessage("Saving...", false);
        JumpNoteWeb.sJsonRpcClient.call(method, paramsJson, new JsonRpcClient.Callback() {
            public void onSuccess(Object data) {
                JSONObject responseJson = (JSONObject) data;
                ModelJso.Note note = (ModelJso.Note) responseJson.get(JumpNoteProtocol.NotesEdit.RET_NOTE)
                        .isObject().getJavaScriptObject();
                JumpNoteWeb.sNotes.put(note.getId(), note);
                JumpNoteWeb.showMessage("Note saved.", true);
                History.newItem("home");
            }

            public void onError(JsonRpcException caught) {
                JumpNoteWeb.showMessage("Error saving: " + caught.getMessage(), false);
            }
        });
    }

    @UiHandler("revertButton")
    void onRevertClick(ClickEvent e) {
        History.newItem("home");
    }
}
