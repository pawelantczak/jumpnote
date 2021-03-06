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

package com.example.jumpnote.allshared;

public final class JumpNoteProtocol {
    /**
     * For server messaging (Android C2DM), prevent feedback to the data change originator.
     */
    public static final String ARG_CLIENT_DEVICE_ID = "client_device_id";

    public static final class ServerInfo {
        public static final String METHOD = "server.info";
        public static final String RET_PROTOCOL_VERSION = "protocol_version";
    }

    public static final class UserInfo {
        public static final String METHOD = "user.info";
        public static final String ARG_LOGIN_CONTINUE = "login_continue";
        public static final String RET_USER = "user";
        public static final String RET_LOGIN_URL = "login_url";
        public static final String RET_LOGOUT_URL = "logout_url";
    }

    public static final class NotesList {
        public static final String METHOD = "notes.list";
        public static final String RET_NOTES = "notes";
    }

    public static final class NotesGet {
        public static final String METHOD = "notes.get";
        public static final String ARG_ID = "id";
        public static final String RET_NOTE = "note";
    }

    public static final class NotesCreate {
        public static final String METHOD = "notes.create";
        public static final String ARG_NOTE = "note";
        public static final String RET_NOTE = "note";
    }

    public static final class NotesEdit {
        public static final String METHOD = "notes.edit";
        public static final String ARG_NOTE = "note";
        public static final String RET_NOTE = "note";
    }

    public static final class NotesDelete {
        public static final String METHOD = "notes.delete";
        public static final String ARG_ID = "id";
    }

    public static final class NotesSync {
        public static final String METHOD = "notes.sync";
        public static final String ARG_SINCE_DATE = "since_date";
        public static final String ARG_LOCAL_NOTES = "local_notes";
        public static final String RET_NOTES = "notes";
        public static final String RET_NEW_SINCE_DATE = "new_since_date";
    }

    public static final class DevicesRegister {
        public static final String METHOD = "devices.register";
        public static final String ARG_DEVICE = "device";
        public static final String RET_DEVICE = "device";
    }

    public static final class DevicesUnregister {
        public static final String METHOD = "devices.unregister";
        public static final String ARG_DEVICE_ID = "device_id";
    }

    public static final class DevicesClear {
        public static final String METHOD = "devices.clear";
    }
}
