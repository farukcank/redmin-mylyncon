package org.svenk.redmine.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.svenk.redmine.core.RedmineCorePlugin;
import org.svenk.redmine.core.exception.RedmineException;

public class RedmineResponseReader {

	private XMLInputFactory inputFactory;
	
	public RedmineResponseReader() {
		inputFactory = new com.ctc.wstx.osgi.InputFactoryProviderImpl().createInputFactory();
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
	}
	
	public String readAuthenticityToken(InputStream in) throws RedmineException {
		try {
			try {
				XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
				inputFactory.createFilteredReader(streamReader, new StreamFilter() {
					public boolean accept(XMLStreamReader reader) {
						if (reader.isStartElement() && reader.getLocalName().equalsIgnoreCase("INPUT") && reader.getAttributeCount()==3) {
							String attribName = reader.getAttributeLocalName(0);
							if (attribName!=null && attribName.equals("name") 
									&& reader.getAttributeValue(0).equals("authenticity_token")
									&& reader.getAttributeLocalName(2).equalsIgnoreCase("value")) {
								return true;
							}
						}
						return false;
					}
				});
				
				//INPUT with name=authenticity_token exists
				if (streamReader.isStartElement()) {
					return streamReader.getAttributeValue(2).trim();
				}
				
			} finally {
				in.close();
			}
		} catch (XMLStreamException e) {
			throw new RedmineException("READING_OF_RESPONSE_ERRORS_FAILED", e);
		} catch (IOException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "STREAM_CLOSING_FAILED");
			StatusHandler.log(status);
		}
		return null;
	}
	
	public Collection<String> readErrors(InputStream in) throws RedmineException {
		Collection<String> errors = null;
		try {
			try {
				XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
				inputFactory.createFilteredReader(streamReader, new StreamFilter() {
					public boolean accept(XMLStreamReader reader) {
						if (reader.isStartElement() && reader.getLocalName().equalsIgnoreCase("DIV") && reader.getAttributeCount()==2) {
							String attribName = reader.getAttributeLocalName(1);
							if (attribName!=null && attribName.equals("id") && reader.getAttributeValue(1).equals("errorExplanation")) {
								return true;
							}
						}
						return false;
					}
				});
				
				//DIV with id=errorExplanation exists
				if (streamReader.isStartElement()) {
					inputFactory.createFilteredReader(streamReader, new StreamFilter() {
						public boolean accept(XMLStreamReader reader) {
							return reader.isStartElement() && reader.getLocalName().equalsIgnoreCase("LI");
						}
					});
					
					//one ore more error notices exists
					if (streamReader.isStartElement()) {
						errors = new ArrayList<String>(5);
						
						while(streamReader.isStartElement()) {
							errors.add(streamReader.getElementText().trim());
							streamReader.nextTag();
						}
					}
				}
				
			} finally {
				in.close();
			}
		} catch (XMLStreamException e) {
			throw new RedmineException("READING_OF_RESPONSE_ERRORS_FAILED", e);
		} catch (IOException e) {
			IStatus status = RedmineCorePlugin.toStatus(e, null, "STREAM_CLOSING_FAILED");
			StatusHandler.log(status);
		}

		return errors;
	}

}
