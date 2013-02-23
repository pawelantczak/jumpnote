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
import com.example.jumpnote.web.client.ModelJso.Note;
import com.example.jumpnote.web.client.controls.NoteItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The notes list screen.
 */
public class NotesList extends Screen {

    private static NotesListUiBinder uiBinder = GWT.create(NotesListUiBinder.class);

    interface NotesListUiBinder extends UiBinder<Widget, NotesList> {
    }

    @UiField
    FlowPanel notesList;

    @UiField
    PushButton createButton;

    public NotesList() {
        initWidget(uiBinder.createAndBindUi(this));
        refreshNotes();
    }

    @UiHandler("createButton")
    void onClick(ClickEvent e) {
        History.newItem("note");
    }

    public void refreshNotes() {
        notesList.clear();
        if (JumpNoteWeb.sNotes.keySet().isEmpty()) {
            Label emptyLabel = new Label();
            emptyLabel.setStyleName("empty");
            emptyLabel.setText("You haven't written any notes, create one by clicking " +
                    "'Create Note' below!");
            notesList.add(emptyLabel);
        } else {
            List<ModelJso.Note> notes = new ArrayList<ModelJso.Note>();
            for (String id : JumpNoteWeb.sNotes.keySet()) {
                ModelJso.Note note = (ModelJso.Note) JumpNoteWeb.sNotes.get(id);
                notes.add(note);
            }

            Collections.sort(notes, new Comparator<ModelJso.Note>() {
                public int compare(Note o1, Note o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });

            for (ModelJso.Note note : notes) {
                NoteItem itemWidget = new NoteItem(note, mNoteItemActionCallback);
                notesList.add(itemWidget);
            }
        }
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        refreshNotes();
        return this;
    }

    private NoteItem.ActionCallback mNoteItemActionCallback = new NoteItem.ActionCallback() {
        public void onEdit(String noteId) {
            History.newItem("note/" + noteId);
        }

        public void onDelete(final String noteId) {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put(JumpNoteProtocol.NotesDelete.ARG_ID, new JSONString(noteId));
            JumpNoteWeb.sJsonRpcClient.call(JumpNoteProtocol.NotesDelete.METHOD, paramsJson, new JsonRpcClient.Callback() {
                public void onSuccess(Object data) {
                    ModelJso.Note note = JumpNoteWeb.sNotes.get(noteId);
                    JumpNoteWeb.sNotes.remove(noteId);
                    JumpNoteWeb.showMessage("Deleted note '" + note.getTitle() + "'", true);
                    refreshNotes();
                }

                public void onError(JsonRpcException caught) {
                    JumpNoteWeb.showMessage("Delete failed: " + caught.getMessage(), false);
                }
            });
        }
    };
}
