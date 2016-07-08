# SystemWideTranslator

 A simple Utility used to translate between the languages I am currently learning.
 Just more convenient than using Google translate all the time.
 
 ### Download:
      https://www.dropbox.com/s/4x4ti1gr6e4q6ei/SystemWideTranslator.jar?dl=0

 ### Usage: 
        copy the text you wish to translate so that it is in the system clip board (Just highlight and press ctrl+c).
        Then press alt+n to get the translation in the currently selected language pairs.

        You can change the base language and the languages to translate to by right clicking the icon in the system tray.

        To make the translation box disappear press alt+m.

        Languages can be easly added by appending them to the two lists in the source code.
        The array acceptedLanguages[] accepts language codes as described by yandex here:
        https://tech.yandex.com/translate/doc/dg/concepts/api-overview-docpage/#languages

        the array acceptedLanguagesFullname[] lists the text that will show up in the menu in the system tray.

 ### First Time Setup:
        The application expects that you will get your own Yandex API key (I can't use mine for all instances of the application)
        And that you will put it in an ASCII encoded file called YKey.txt

        This file must be located in the same folder as the jar file.

 Created by Nicholas on 2016-06-04.

 #### Copyright Nicholas Mio 2016

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
