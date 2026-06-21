/*
 * MIMEhandler.java
 *
 * Copyright (C) 2012 Pär Crusefalk (SM0RWO)
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package javaradiomsg;

/**
 * This class is used to handle MIME email. Initially I will use it to create MIME encoded
 * emails and later I will feed it raw email data to chew on and later extract parsed data.
 *
 * @author per
 */
public class MIMEhandler {
    // The building blocks of the MIME message
    private String MIMEHeader="MIME-Version: 1.0";
    private String ContentID ="Content-ID: ";
    private String ContentType = "Content-Type: ";
    private String TextType = "text/plain";
    private String QuotType = "quoted-printable";
    private String MixedType = "multipart/mixed";
    private String AlterType = "multipart/alternative";
    private String HTMLtype = "text/html";
    private String DispositAtt = "Content-Disposition: attachment;";
    private String ContentEncod ="content-transfer-encoding:";

}
