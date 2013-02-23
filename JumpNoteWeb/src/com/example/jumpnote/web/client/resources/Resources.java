
package com.example.jumpnote.web.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the web client.
 */
public interface Resources extends ClientBundle {
    @Source("btn_create_note.png")
    ImageResource btn_create();

    @Source("btn_create_note_on.png")
    ImageResource btn_create_on();

    @Source("btn_delete.png")
    ImageResource btn_delete();

    @Source("btn_delete_on.png")
    ImageResource btn_delete_on();

    @Source("btn_revert.png")
    ImageResource btn_revert();

    @Source("btn_revert_on.png")
    ImageResource btn_revert_on();

    @Source("btn_edit.png")
    ImageResource btn_edit();

    @Source("btn_edit_on.png")
    ImageResource btn_edit_on();

    @Source("btn_save.png")
    ImageResource btn_save();

    @Source("btn_save_on.png")
    ImageResource btn_save_on();

    public interface Style extends CssResource {
        String mainBlock();

        String nameSpan();
    }
}
