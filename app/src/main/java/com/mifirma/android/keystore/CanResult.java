package com.mifirma.android.keystore;

import android.util.Log;

import com.mifirma.android.logic.Ilp;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import es.gob.jmulticard.card.dnie.CustomAuthorizeCallback;
import es.gob.jmulticard.card.dnie.CustomTextInputCallback;

public final class CanResult implements CallbackHandler{

    private String can = null;
    private String pin = null;
    private final Ilp ilp;

    public CanResult(final Ilp relatedIlp) {
        this.ilp = relatedIlp;
    }

    public Ilp getIlp() {
        return ilp;
    }

    public void setCan(final String c) {
        can = c;
    }

    public void setPin(final String p) {
        pin = p;
    }

    public CallbackHandler getCallbackHandler() {
        return this;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks != null) {
            for (final Callback cb : callbacks) {
                if (cb != null) {
                    // CAN
                    if (cb instanceof CustomTextInputCallback) {
                        ((CustomTextInputCallback)cb).setText(can);
                        return;
                    }
                    else if (cb instanceof CustomAuthorizeCallback) {
                        ((CustomAuthorizeCallback)cb).setAuthorized(true);
                        return;
                    }
                    // PIN
                    else if (cb instanceof PasswordCallback) {
                        ((PasswordCallback)cb).setPassword(pin != null ? pin.toCharArray() : null);
                        return;
                    }
                    else {
                        Log.e("com.mifirma", "Callback no soportada: " + cb.getClass().getName()); //$NON-NLS-1$
                    }
                }
            }
        }
        else {
            Log.w("com.mifirma", "Se ha revibido un array de Callbacks nulo"); //$NON-NLS-1$
        }
        throw new UnsupportedCallbackException(null);
    }
}
