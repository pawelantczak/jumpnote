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

package com.example.jumpnote.web.client.controls;

import com.example.jumpnote.allshared.Model;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * A list item widget, representing a note.
 */
public class NoteItem extends Composite {

    private static NoteItemUiBinder uiBinder = GWT.create(NoteItemUiBinder.class);

    interface NoteItemUiBinder extends UiBinder<Widget, NoteItem> {
    }

    @UiField
    Label titleLabel;

    @UiField
    Label snippetLabel;

    @UiField
    PushButton editButton;

    @UiField
    PushButton deleteButton;

    Model.Note mNote;

    ActionCallback mActionCallback;

    public NoteItem(Model.Note note, ActionCallback actionCallback) {
        initWidget(uiBinder.createAndBindUi(this));

        this.mNote = note;
        titleLabel.setText(note.getTitle());
        snippetLabel.setText(note.getBody());

        mActionCallback = actionCallback;

        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            mActionCallback.onEdit(mNote.getId());
            event.stopPropagation();
            event.preventDefault();
            return;
        }
        super.onBrowserEvent(event);
    }

    @UiHandler("editButton")
    void onEditClick(ClickEvent e) {
        e.stopPropagation();
        mActionCallback.onEdit(mNote.getId());
    }

    @UiHandler("deleteButton")
    void onDeleteClick(ClickEvent e) {
        e.stopPropagation();
        if (Window.confirm("Are you sure you want to delete '" + mNote.getTitle() + "'?")) {
            mActionCallback.onDelete(mNote.getId());
        }
    }

    public interface ActionCallback {
        public void onEdit(String noteId);
        public void onDelete(String noteId);
    }
}
