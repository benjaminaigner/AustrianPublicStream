/**
 Copyright:
 2009 Geoff Bruckner

 This file is part of AustrianPublicStream.

 AustrianPublicStream is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 AustrianPublicStream is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with AustrianPublicStream.  If not, see <http://www.gnu.org/licenses/>.
 **/

package systems.byteswap.publicstream;

import android.os.Binder;

import java.lang.ref.WeakReference;

/**
 * A generic implementation of Binder to be used for local services
 * @author Geoff Bruckner  12th December 2009
 *
 * @param <S> The type of the service being bound
 */

public class LocalBinder<S> extends Binder {
    private WeakReference<S> mService;


    public LocalBinder(S service){
        //mService = new WeakReference<S>(service);
        mService = new WeakReference<>(service);
    }


    public S getService() {
        return mService.get();
    }
}
