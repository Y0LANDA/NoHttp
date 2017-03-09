/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp;

/**
 * Created in Jan 31, 2016 8:45:37 PM.
 *
 * @author Yan Zhenjie.
 */
public interface RedirectHandler {

    /**
     * When the server's response code is 302 or 303 corresponding need to redirect is invoked.
     *
     * @param responseHeaders the service side head accordingly.
     * @return {@link IBasicRequest}.
     */
    IBasicRequest onRedirect(Headers responseHeaders);

    /**
     * Whether to allow the redirection, if not redirect will not be {@code #onRedirect(Headers)} callback method, at
     * the same time will ban NoHttp automatic redirection.If allowed to redirect, first
     * call {@code #onRedirect(Headers)} method, if {@code #onRedirect(Headers)} method returns null, execute NoHttp
     * default redirect.
     *
     * @param responseHeaders the service side head accordingly.
     * @return returns true said allow redirection, returns false said do not allow the redirection.
     */
    boolean isDisallowedRedirect(Headers responseHeaders);
}
