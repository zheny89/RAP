package ldap;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

/**
 * ������ � Active Directory � ������� Java
 * 
 * ������ ������, ������� ��� ����� ������������
 * o			Organization
 * ou			Organizational unit
 * cn			Common name
 * sn			Surname
 * givenname	First name
 * uid			Userid
 * dn			Distinguished name
 * mail			Email address
 * 
 * @filename LdapAuthentication.java
 * @author assuslova
 */

public class LdapAuthentication {

    /**
     * ���������� LDAP � ������������ ������, ����� ���������� �������
     */
    private static LdapContext ldapContext;
    private static Attributes attributes;
    private static String email = null;
    private static String commonName = null;

    private static final Logger LOG = Logger.getLogger(LdapAuthentication.class);
    private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static String[] returnedAttributes = { "cn", "mail" };
    private static String[] returnSN = { "cn" };

    private LdapAuthentication() {
        
    }
    
    /**
     * 
     * @param username
     * @param password
     * @return
     * @throws NamingException
     */
    public static LdapContext getConnection(String username, String password) throws NamingException {
        return getConnection(username, password, null, null);
    }

    /**
     * 
     * @param username
     * @param password
     * @param domainName
     * @return
     * @throws NamingException
     */
    public static LdapContext getConnection(String username, String password, String domainName)
            throws NamingException {
        return getConnection(username, password, domainName, null);
    }

    /**
     * �������� ��������� � �������
     * 
     * @param username
     *            �������� ��� �������� ������������
     *            System.getProperty("user.name")
     * @param password
     *            ������ ������ ��� ������
     * @param domainName
     *            ����� ����� ������������ � ����������, ����� ��������� �� ���������
     * @param serverName
     *            �� ������������ ��������
     * @return
     * @throws NamingException
     */
    public static LdapContext getConnection(String username, String password, String domainName, String serverName)
            throws NamingException {

        String checkedDomainName = checkDomainName(domainName);
        
        String trimmedPassword = trimPassword(password);

        /**
         * ��������� � �������������� ���������� ����� ������������ / �������
         * Hashtable ������������ ��� �������� � ����������� InitialLdapContext 
         */
        Hashtable<String, String> properties = new Hashtable<>();

        String principalName = username + "@" + checkedDomainName;
        properties.put(Context.SECURITY_PRINCIPAL, principalName);

        if (trimmedPassword != null)
            properties.put(Context.SECURITY_CREDENTIALS, trimmedPassword);

        // ldap://chel.rusoft.local/
        String ldapURL = "ldap://" + ((serverName == null) ? checkedDomainName : serverName + "." + checkedDomainName) + '/';

        properties.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        properties.put(Context.PROVIDER_URL, ldapURL);
        properties.put(Context.REFERRAL, "follow");

        try {
            ldapContext = new InitialLdapContext(properties, null);
        } catch (CommunicationException e) {
            throw new NamingException(
                    "Failed to connect to " + checkedDomainName + ((serverName == null) ? "" : " through " + serverName));
        } catch (NamingException e) {
            throw new NamingException("Failed to authenticate " + username + "@" + checkedDomainName
                    + ((serverName == null) ? "" : " through " + serverName));
        }
        return ldapContext;
    }

    private static String trimPassword(String password) {
        String trimmedPassword = password;
        // ������ ������ ������� �� ������
        if (trimmedPassword != null) {
            trimmedPassword = trimmedPassword.trim();
            if (trimmedPassword.length() == 0)
                trimmedPassword = null;
        }
        return trimmedPassword;
    }

    private static String checkDomainName(String domainName) {
        /**
         * ���� �� ����� �����, �� ��������� ���
         */
        
        String checkedName = domainName;
        
        if (checkedName == null) {
            try {
                // hostname.chel.rusoft.local
                String hostname = InetAddress.getLocalHost().getCanonicalHostName();
                // ������� ��� ����� chel.rusoft.local
                if (hostname.split("\\.").length > 1)
                    checkedName = hostname.substring(hostname.indexOf('.') + 1);
            } catch (UnknownHostException e) {
                // TODO ��������� ����������
                LOG.error(e.getMessage(), e);
            }
        }
        return checkedName;
    }

    /**
     * ��������� ��������� ������������ ( � ������ ������ ����� ������ email � cn(������ ��� ������������) )
     * 
     * @param username
     *            ��� ������������
     * @param context
     *            ���������� � �������
     */
    public static void getUsersAttribute(String username, LdapContext context) {
        try {
            String domainName = null;
            String authenticatedUser = (String) context.getEnvironment().get(Context.SECURITY_PRINCIPAL);
            if (authenticatedUser.contains("@")) {
                domainName = authenticatedUser.substring(authenticatedUser.indexOf('@') + 1);
            }

            if (domainName != null) {
                String principalName = username + "@" + domainName;

                // ������������� ���������� �������
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SUBTREE_SCOPE);
                // �������������, ����� ��������� ����� �������
                controls.setReturningAttributes(returnSN);

                // baseDN, ������ � controls
                NamingEnumeration<SearchResult> answerForSN = context.search(baseDN(domainName),
                        "(& (userPrincipalName=" + principalName + "))", controls);

                // ������ ��� cn � �� ���� ����� ������ mail
                while (answerForSN.hasMore()) {
                    Attributes attr = answerForSN.next().getAttributes();
                    commonName = (String) attr.get("cn").get();
                }

                controls.setReturningAttributes(returnedAttributes);
                //����� �������� ���� ������ ������ ������
                NamingEnumeration<SearchResult> answer = context.search(baseDN(domainName),
                        "(& (mail=*) (cn=" + commonName + "))", controls);

                if (answer.hasMore()) {
                    attributes = answer.next().getAttributes();
                    email = (String) attributes.get("mail").get();
                }
            }
        } catch (NamingException e) {
            // TODO ��������� ����������
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * �� chel.rusoft.local �������� DC=chel,DC=rusoft,DC=local
     * 
     * @param domainName
     *            ��� ����������� ������
     * @return ������� ���
     */
    private static String baseDN(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0)
                continue;
            if (buf.length() > 0)
                buf.append(",");
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }

    /**
     * ��������� ���������� LDAP � ������������ ������
     */
    public static void closeLdapConnection() {
        try {
            if (ldapContext != null)
                ldapContext.close();
        } catch (NamingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static Attributes getAttributes() {
        return attributes;
    }

    public static String getCommonName() {
        return commonName;
    }

    public static String getEmail() {
        return email;
    }

}
