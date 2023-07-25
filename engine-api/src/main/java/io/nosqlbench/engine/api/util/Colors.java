/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.util;

/**
 * ANSI console colors for enhanced logging
 */
public class Colors {
    public static String ANSI_Black = (char) 27 + "[30m";
    public static String ANSI_Red = (char) 27 + "[31m";
    public static String ANSI_Green = (char) 27 + "[32m";
    public static String ANSI_Yellow = (char) 27 + "[33m";
    public static String ANSI_Blue = (char) 27 + "[34m";
    public static String ANSI_Magenta = (char) 27 + "[35m";
    public static String ANSI_Cyan = (char) 27 + "[36m";
    public static String ANSI_White = (char) 27 + "[37m";

    public static String ANSI_BlackBG = (char) 27 + "[40m";
    public static String ANSI_RedBG = (char) 27 + "[41m";
    public static String ANSI_GreenBG = (char) 27 + "[42m";
    public static String ANSI_YellowBG = (char) 27 + "[43m";
    public static String ANSI_BlueBG = (char) 27 + "[44m";
    public static String ANSI_MagentaBG = (char) 27 + "[45m";
    public static String ANSI_CyanBG = (char) 27 + "[46m";
    public static String ANSI_WhiteBG = (char) 27 + "[47m";

    public static String ANSI_BrightBlack = (char) 27 + "[90m";
    public static String ANSI_BrightRed = (char) 27 + "[91m";
    public static String ANSI_BrightGreen = (char) 27 + "[92m";
    public static String ANSI_BrightYellow = (char) 27 + "[93m";
    public static String ANSI_BrightBlue = (char) 27 + "[94m";
    public static String ANSI_BrightMagenta = (char) 27 + "[95m";
    public static String ANSI_BrightCyan = (char) 27 + "[96m";
    public static String ANSI_BrightWhite = (char) 27 + "[97m";

    public static String ANSI_BrightBlackBG = (char) 27 + "[100m";
    public static String ANSI_BrightRedBG = (char) 27 + "[101m";
    public static String ANSI_BrightGreenBG = (char) 27 + "[102m";
    public static String ANSI_BrightYellowBG = (char) 27 + "[103m";
    public static String ANSI_BrightBlueBG = (char) 27 + "[104m";
    public static String ANSI_BrightMagentaBG = (char) 27 + "[105m";
    public static String ANSI_BrightCyanBG = (char) 27 + "[106m";
    public static String ANSI_BrightWhiteBG = (char) 27 + "[107m";

    public static String ANSI_Reset = (char) 27 + "[39;49m";
}
