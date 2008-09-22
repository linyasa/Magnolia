// ** I18N

// Calendar FI language (Finnish, Suomi)
// Author: Jarno K�yhk�, <gambler@phnet.fi>
// Encoding: UTF-8
// Distributed under the same terms as the calendar itself.

// full day names
Calendar._DN = new Array
("sunnuntai",
 "maanantai",
 "tiistai",
 "keskiviikko",
 "torstai",
 "perjantai",
 "lauantai",
 "sunnuntai");

// short day names
Calendar._SDN = new Array
("su",
 "ma",
 "ti",
 "ke",
 "to",
 "pe",
 "la",
 "su");

// full month names
Calendar._MN = new Array
("tammikuu",
 "helmikuu",
 "maaliskuu",
 "huhtikuu",
 "toukokuu",
 "kes�kuu",
 "hein�kuu",
 "elokuu",
 "syyskuu",
 "lokakuu",
 "marraskuu",
 "joulukuu");

// short month names
Calendar._SMN = new Array
("tam",
 "hel",
 "maa",
 "huh",
 "tou",
 "kes",
 "hei",
 "elo",
 "syy",
 "lok",
 "mar",
 "jou");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Tietoja kalenterista";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"Uusin versio osoitteessa: http://www.dynarch.com/projects/calendar/\n" +
"Julkaistu GNU LGPL lisenssin alaisuudessa. Lis�tietoja osoitteessa http://gnu.org/licenses/lgpl.html" +
"\n\n" +
"P�iv�m��r�valinta:\n" +
"- K�yt� \xab, \xbb painikkeita valitaksesi vuosi\n" +
"- K�yt� " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " painikkeita valitaksesi kuukauden\n" +
"- Pit�m�ll� hiiren painiketta mink� tahansa yll� olevan painikkeen kohdalla, saat n�kyviin valikon nopeampaan siirtymiseen.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Ajan valinta:\n" +
"- Klikkaa kellonajan numeroita lis�t�ksesi aikaa\n" +
"- tai pit�m�ll� Shift-n�pp�int� pohjassa saat aikaa taaksep�in\n" +
"- tai klikkaa ja pid� hiiren painike pohjassa sek� liikuta hiirt� muuttaaksesi aikaa nopeasti eteen- ja taaksep�in.";

Calendar._TT["PREV_YEAR"] = "Edell. vuosi (paina hetki, n�et valikon)";
Calendar._TT["PREV_MONTH"] = "Edell. kuukausi (paina hetki, n�et valikon)";
Calendar._TT["GO_TODAY"] = "Siirry t�h�n p�iv��n";
Calendar._TT["NEXT_MONTH"] = "Seur. kuukausi (paina hetki, n�et valikon)";
Calendar._TT["NEXT_YEAR"] = "Seur. vuosi (paina hetki, n�et valikon)";
Calendar._TT["SEL_DATE"] = "Valitse p�iv�m��r�";
Calendar._TT["DRAG_TO_MOVE"] = "Siirr� kalenteria";
Calendar._TT["PART_TODAY"] = " (t�n��n)";
// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "N�yt� %s ensimm�isen�";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";
Calendar._TT["CLOSE"] = "Sulje";
Calendar._TT["TODAY"] = "T�n��n";
Calendar._TT["TIME_PART"] = "(Shift-) Klikkaa tai liikuta muuttaaksesi arvoa";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d.%m.%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%d.%m.%Y";

Calendar._TT["WK"] = "vko";
Calendar._TT["TIME"] = "klo:";
