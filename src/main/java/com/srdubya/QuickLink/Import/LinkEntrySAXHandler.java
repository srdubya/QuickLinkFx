package com.srdubya.QuickLink.Import;

import com.srdubya.QuickLink.LinkEntry;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class LinkEntrySAXHandler extends DefaultHandler {
    /* -- JSON will look like this
    {
      "Name":"___AppData",
      "Login":"",
      "Email":"",
      "Path":"C:\\Users\\user\\AppData",
      "App":"",
      "IsCloseOnAccess":true,
      "EncryptedPassword":""
      }
     */

    private LinkEntry newLinkEntry;
    private String content = null;
    private final ImportController.NewLinkEntryHandler entryHandler;

    LinkEntrySAXHandler(ImportController.NewLinkEntryHandler newLinkEntryHandler) {
        entryHandler = newLinkEntryHandler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if(qName.equals("FavoriteLink")) {
            newLinkEntry = new LinkEntry();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if(newLinkEntry != null) {
            switch (qName) {
                case "Name":
                    newLinkEntry.setName(content);
                    break;
                case "Login":
                    newLinkEntry.setLogin(content);
                    break;
                case "Email":
                    newLinkEntry.setEmail(content);
                    break;
                case "Path":
                    newLinkEntry.setPath(content);
                    break;
                case "App":
                    newLinkEntry.setApp(content);
                    break;
                case "IsCloseOnAccess":
                    newLinkEntry.setClose(content.equalsIgnoreCase("true"));
                    break;
                case "EncryptedPassword":
                    newLinkEntry.setPassword(content);
                    break;
                case "FavoriteLink":
                    entryHandler.operation(newLinkEntry);
                    newLinkEntry = null;
                    break;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        content = String.copyValueOf(ch, start, length).trim();
    }
}
