package info.magnolia.cms.core.ie;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.ie.filters.ImportXmlRootFilter;
import info.magnolia.cms.core.ie.filters.MagnoliaV2Filter;
import info.magnolia.cms.core.ie.filters.VersionFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * imports and exports XML data
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 * @author Oliver Lietz
 */
public class DataTransporter {

    private static final int INDENT_VALUE = 2;

    private static Logger log = LoggerFactory.getLogger(DataTransporter.class.getName());

    final static int bootstrapImportMode = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;

    public static final String ZIP = ".zip";

    public static final String GZ = ".gz";

    public static final String XML = ".xml";

    public static final String DOT = ".";

    public static final String SLASH = "/";

    public static final String JCR_ROOT = "jcr:root";

    /**
     * Document -> File
     * @param xmlDocument uploaded file
     * @param repositoryName selected repository
     * @param basepath base path in repository
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @param saveAfterImport
     * @param createBasepathIfNotExist
     * @throws IOException
     * @see ImportUUIDBehavior
     */
    public static synchronized void importDocument(Document xmlDocument, String repositoryName, String basepath,
                                                   boolean keepVersionHistory, int importMode, boolean saveAfterImport,
                                                   boolean createBasepathIfNotExist)
            throws IOException {
        File xmlFile = xmlDocument.getFile();
        importFile(xmlFile, basepath, repositoryName, keepVersionHistory, importMode, saveAfterImport,
                createBasepathIfNotExist);
    }

    /**
     * File -> InputStream
     * @param xmlFile (zipped/gzipped) XML file to import
     * @param repositoryName selected repository
     * @param basepath base path in repository
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @param saveAfterImport
     * @param createBasepathIfNotExist
     * @throws IOException
     * @see ImportUUIDBehavior
     */
    public static synchronized void importFile(File xmlFile, String repositoryName, String basepath,
                                               boolean keepVersionHistory, int importMode, boolean saveAfterImport,
                                               boolean createBasepathIfNotExist)
            throws IOException {
        String name = xmlFile.getAbsolutePath();
        InputStream xmlStream = getInputStreamForFile(xmlFile);
        importXmlStream(xmlStream, repositoryName, basepath, name, keepVersionHistory, importMode, saveAfterImport,
                createBasepathIfNotExist);
    }

    /**
     * @param xmlFile
     * @param repositoryName
     * @throws IOException
     */
    public static void executeBootstrapImport(File xmlFile, String repositoryName) throws IOException {
        String filenameWithoutExt = StringUtils.substringBeforeLast(xmlFile.getName(), DOT);
        if (filenameWithoutExt.endsWith(XML)) {
            // if file ends in .xml.gz or .xml.zip
            // need to keep the .xml to be able to view it after decompression
            filenameWithoutExt = StringUtils.substringBeforeLast(xmlFile.getName(), DOT);
        }
        String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(filenameWithoutExt, DOT), DOT);
        String basepath = SLASH + StringUtils.replace(pathName, DOT, SLASH);
        DataTransporter.importFile(xmlFile, repositoryName, basepath, false, bootstrapImportMode, true, true);
    }

    /**
     * imports XML stream into repository<p/>
     * XML is filtered by <code>MagnoliaV2Filter</code>, <code>VersionFilter</code> and <code>ImportXmlRootFilter</code>
     * if <code>keepVersionHistory</code> is set to <code>false</code>
     * @param xmlStream XML stream to import
     * @param repositoryName selected repository
     * @param basepath base path in repository
     * @param name (absolute path of <code>File</code>)
     * @param keepVersionHistory if <code>false</code> version info will be stripped before importing the document
     * @param importMode a valid value for ImportUUIDBehavior
     * @param saveAfterImport
     * @param createBasepathIfNotExist
     * @throws IOException
     * @see ImportUUIDBehavior
     * @see ImportXmlRootFilter
     * @see VersionFilter
     * @see MagnoliaV2Filter
     */
    public static synchronized void importXmlStream(InputStream xmlStream, String repositoryName, String basepath,
                                                    String name, boolean keepVersionHistory, int importMode,
                                                    boolean saveAfterImport, boolean createBasepathIfNotExist)
            throws IOException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Workspace ws = hm.getWorkspace();

        if (log.isDebugEnabled()) {
            log.debug("Importing content into repository: [{}] from: [{}] into path: [{}]", //$NON-NLS-1$
                    new Object[]{repositoryName, name, basepath});
        }

        if (!hm.isExist(basepath) && createBasepathIfNotExist) {
            try {
                ContentUtil.createPath(hm, basepath, ItemType.CONTENT);
            }
            catch (RepositoryException e) {
                log.error("can't create path [{}]", basepath); //$NON-NLS-1$
            }
        }

        Session session = ws.getSession();

        try {
            if (keepVersionHistory) {
                // do not manipulate
                session.importXML(basepath, xmlStream, importMode);
            }
            else {
                // create readers/filters and chain
                XMLReader initialReader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

                XMLFilter magnoliaV2Filter = null;

                // if stream is from regular file, test for belonging XSL file to apply XSL transformation to XML
                if (new File(name).isFile()) {
                    InputStream xslStream  = getXslStreamForXmlFile(new File(name));
                    if (xslStream != null) {
                        Source xslSource = new StreamSource(xslStream);
                        SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                        XMLFilter xslFilter = saxTransformerFactory.newXMLFilter(xslSource);
                        magnoliaV2Filter = new MagnoliaV2Filter(xslFilter);
                    }
                }

                if (magnoliaV2Filter == null) {
                    magnoliaV2Filter = new MagnoliaV2Filter(initialReader);
                }

                XMLFilter versionFilter = new VersionFilter(magnoliaV2Filter);
                XMLReader finalReader = new ImportXmlRootFilter(versionFilter);

                ContentHandler handler = session.getImportContentHandler(basepath, importMode);
                finalReader.setContentHandler(handler);

                // parse XML, import is done by handler from session
                try {
                    finalReader.parse(new InputSource(xmlStream));
                }
                finally {
                    IOUtils.closeQuietly(xmlStream);
                }

                if (((ImportXmlRootFilter) finalReader).rootNodeFound) {
                    String path = basepath;
                    if (!path.endsWith(SLASH)) {
                        path += SLASH;
                    }

                    Node dummyRoot = (Node) session.getItem(path + JCR_ROOT);
                    for (Iterator iter = dummyRoot.getNodes(); iter.hasNext();) {
                        Node child = (Node) iter.next();
                        // move childs to real root

                        if (session.itemExists(path + child.getName())) {
                            session.getItem(path + child.getName()).remove();
                        }

                        session.move(child.getPath(), path + child.getName());
                    }
                    // delete the dummy node
                    dummyRoot.remove();
                }
            }
        }
        catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(xmlStream);
        }

        try {
            if (saveAfterImport) {
                session.save();
            }
        }
        catch (RepositoryException e) {
            log.error(MessageFormat.format(
                    "Unable to save changes to the [{0}] repository due to a {1} Exception: {2}.", //$NON-NLS-1$
                    new Object[]{repositoryName, e.getClass().getName(), e.getMessage()}), e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * @param file
     * @return XSL stream for Xml file or <code>null</code>
     */
    protected static InputStream getXslStreamForXmlFile(File file) {
        InputStream xslStream = null;
        String xlsFilename = StringUtils.substringBeforeLast(file.getAbsolutePath(), ".") + ".xsl"; //$NON-NLS-1$
        File xslFile = new File(xlsFilename);
        if (xslFile.exists()) {
            try {
                xslStream = new FileInputStream(xslFile);
                log.info("XSL file for [" + file.getName() + "] found (" + xslFile.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (FileNotFoundException e) { // should never happen (xslFile.exists())
                e.printStackTrace();
            }
        }
        return xslStream;
    }

    /**
     * creates a stream from the (zipped/gzipped) XML file
     * @param xmlFile
     * @return stream of the file
     * @throws IOException
     */
    private static InputStream getInputStreamForFile(File xmlFile) throws IOException {
        InputStream xmlStream = null;
        // looks like the zip one is buggy. It throws exception when trying to use it
        if (xmlFile.getName().endsWith(ZIP)) {
            xmlStream = new ZipInputStream((new FileInputStream(xmlFile)));
        }
        else if (xmlFile.getName().endsWith(GZ)) {
            xmlStream = new GZIPInputStream((new FileInputStream(xmlFile)));
        }
        else { // if(fileName.endsWith(XML))
            xmlStream = new FileInputStream(xmlFile);
        }
        return xmlStream;
    }


    public static void executeExport(OutputStream baseOutputStream, boolean keepVersionHistory, boolean format,
                                     Session session, String basepath, String repository, String ext) throws IOException {
        OutputStream outputStream = baseOutputStream;
        if (ext.endsWith(ZIP)) {
            outputStream = new ZipOutputStream(baseOutputStream);
        }
        else if (ext.endsWith(GZ)) {
            outputStream = new GZIPOutputStream(baseOutputStream);
        }

        try {
            if (keepVersionHistory) {
                // use exportSystemView in order to preserve property types
                // http://issues.apache.org/jira/browse/JCR-115
                if (!format) {
                    session.exportSystemView(basepath, outputStream, false, false);
                }
                else {
                    parseAndFormat(outputStream, null, repository, basepath, session);
                }
            }
            else {
                // use XMLSerializer and a SAXFilter in order to rewrite the
                // file
                XMLReader reader = new VersionFilter(XMLReaderFactory
                        .createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName()));
                parseAndFormat(outputStream, reader, repository, basepath, session);
            }
        }
        catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        catch (SAXException e) {
            throw new NestableRuntimeException(e);
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }

        // finish the stream properly if zip stream
        // this is not done by the IOUtils
        if (outputStream instanceof DeflaterOutputStream) {
            ((DeflaterOutputStream) outputStream).finish();
        }

        baseOutputStream.flush();
        IOUtils.closeQuietly(baseOutputStream);
    }

    /**
     * This export the content of the repository, and format it if necessary
     * @param stream the stream to write the content to
     * @param reader the reader to use to parse the xml content (so that we can perform filtering), if null instanciate
     * a default one
     * @param repository the repository to export
     * @param basepath the basepath in the repository
     * @param session the session to use to export the data from the repository
     * @throws IOException
     * @throws SAXException
     * @throws RepositoryException
     * @throws PathNotFoundException
     */
    public static void parseAndFormat(OutputStream stream, XMLReader reader, String repository, String basepath,
                                      Session session) throws IOException, SAXException, PathNotFoundException, RepositoryException {

        if (reader == null) {
            reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());
        }

        // write to a temp file and then re-read it to remove version history
        File tempFile = File.createTempFile("export-" + repository + session.getUserID(), "xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream fileStream = new FileOutputStream(tempFile);

        try {
            session.exportSystemView(basepath, fileStream, false, false);
        }
        finally {
            IOUtils.closeQuietly(fileStream);
        }

        readFormatted(reader, tempFile, stream);

        if (!tempFile.delete()) {
            log.warn("Could not delete temporary export file {}", tempFile.getAbsolutePath()); //$NON-NLS-1$
        }
    }

    /**
     * @param reader
     * @param inputFile
     * @param outputStream
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     */
    protected static void readFormatted(XMLReader reader, File inputFile, OutputStream outputStream)
            throws FileNotFoundException, IOException, SAXException {
        InputStream fileInputStream = new FileInputStream(inputFile);
        readFormatted(reader, fileInputStream, outputStream);
        IOUtils.closeQuietly(fileInputStream);
    }

    /**
     * @param reader
     * @param inputStream
     * @param outputStream
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     */
    protected static void readFormatted(XMLReader reader, InputStream inputStream, OutputStream outputStream)
            throws FileNotFoundException, IOException, SAXException {

        OutputFormat outputFormat = new OutputFormat();

        outputFormat.setPreserveSpace(false); // this is ok, doesn't affect text nodes??
        outputFormat.setIndenting(true);
        outputFormat.setIndent(INDENT_VALUE);
        outputFormat.setLineWidth(120); // need to be set after setIndenting()!

        reader.setContentHandler(new XMLSerializer(outputStream, outputFormat));
        reader.parse(new InputSource(inputStream));

        IOUtils.closeQuietly(inputStream);
    }

}
