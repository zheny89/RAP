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
 * «апрос к Active Directory с помощью Java
 * 
 * пример данных, которые нам могут понадобитьс€
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
     * соединение LDAP с контроллером домена, потом необходимо закрыть
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
     * —оздание коннекции с доменом
     * 
     * @param username
     *            получаем им€ текущего пользовател€
     *            System.getProperty("user.name")
     * @param password
     *            просим ввести его пароль
     * @param domainName
     *            домен можен передаватьс€ в параметрах, иначе определим из окружени€
     * @param serverName
     *            не об€зательный параметр
     * @return
     * @throws NamingException
     */
    public static LdapContext getConnection(String username, String password, String domainName, String serverName)
            throws NamingException {

        String checkedDomainName = checkDomainName(domainName);
        
        String trimmedPassword = trimPassword(password);

        /**
         * —в€зываем с использованием указанного имени пользовател€ / паролем
         * Hashtable используетс€ дл€ передачи в конструктор InitialLdapContext 
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
        // ”далим лишние символы из парол€
        if (trimmedPassword != null) {
            trimmedPassword = trimmedPassword.trim();
            if (trimmedPassword.length() == 0)
                trimmedPassword = null;
        }
        return trimmedPassword;
    }

    private static String checkDomainName(String domainName) {
        /**
         * ≈сли не ввели домен, то определим его
         */
        
        String checkedName = domainName;
        
        if (checkedName == null) {
            try {
                // hostname.chel.rusoft.local
                String hostname = InetAddress.getLocalHost().getCanonicalHostName();
                // выделим сам домен chel.rusoft.local
                if (hostname.split("\\.").length > 1)
                    checkedName = hostname.substring(hostname.indexOf('.') + 1);
            } catch (UnknownHostException e) {
                // TODO обработка исключений
                LOG.error(e.getMessage(), e);
            }
        }
        return checkedName;
    }

    /**
     * ѕолучение атрибутов пользовател€ ( в данном случае хотим узнать email и cn(полное им€ пользовател€) )
     * 
     * @param username
     *            »м€ пользовател€
     * @param context
     *            —оединение с доменом
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

                // инициализаци€ управлени€ поиском
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SUBTREE_SCOPE);
                // устанавливаем, какие параметры хотим вернуть
                controls.setReturningAttributes(returnSN);

                // baseDN, фильтр и controls
                NamingEnumeration<SearchResult> answerForSN = context.search(baseDN(domainName),
                        "(& (userPrincipalName=" + principalName + "))", controls);

                // узнаем наш cn и по нему потом найдем mail
                while (answerForSN.hasMore()) {
                    Attributes attr = answerForSN.next().getAttributes();
                    commonName = (String) attr.get("cn").get();
                }

                controls.setReturningAttributes(returnedAttributes);
                //можем задавать свой фильтр поиска данных
                NamingEnumeration<SearchResult> answer = context.search(baseDN(domainName),
                        "(& (mail=*) (cn=" + commonName + "))", controls);

                if (answer.hasMore()) {
                    attributes = answer.next().getAttributes();
                    email = (String) attributes.get("mail").get();
                }
            }
        } catch (NamingException e) {
            // TODO ќбработка исключений
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * »з chel.rusoft.local получаем DC=chel,DC=rusoft,DC=local
     * 
     * @param domainName
     *            им€ контроллера домена
     * @return базовое им€
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
     * «акрывает соединение LDAP с контроллером домена
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
