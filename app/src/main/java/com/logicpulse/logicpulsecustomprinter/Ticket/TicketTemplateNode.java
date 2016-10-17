package com.logicpulse.logicpulsecustomprinter.Ticket;

/**
 * Created by mario.monteiro on 06/10/2016.
 */

public class TicketTemplateNode {

    private String key;
    //Common
    private String type;
    private String value;
    private Integer feeds = 0;
    private Boolean enabled = true;
    //Text
    //FONT_TYPE_A = 0;
    //FONT_TYPE_B = 1;
    private Integer charfont = 0;
    //FONT_SIZE_X1 = 0;
    //FONT_SIZE_X2 = 1;
    //FONT_SIZE_X3 = 2;
    //FONT_SIZE_X4 = 3;
    //FONT_SIZE_X5 = 4;
    //FONT_SIZE_X6 = 5;
    //FONT_SIZE_X7 = 6;
    //FONT_SIZE_X8 = 7;
    private Integer charheight = 0;
    private Integer charwidth = 0;
    private Boolean emphasized = false;
    private Boolean italic = false;
    private Boolean underline = false;
    //FONT_CS_DEFAULT = 0;
    //FONT_CS_RUSSIAN = 1;
    //FONT_CS_TURKISH = 2;
    //FONT_CS_EASTEEUROPE = 3;
    //FONT_CS_ISRAELI = 4;
    //FONT_CS_GREEK = 5;
    private Integer charset = 0;
    //Image
    //IMAGE_ALIGN_TO_LEFT = 0; (use -1)
    //IMAGE_ALIGN_TO_CENTER = -1 (use 0);
    //IMAGE_ALIGN_TO_RIGHT = -2 (use 1);
    private Integer align = -1;
    private String source = "assets";
    //IMAGE_SCALE_NONE = 0;
    //IMAGE_SCALE_TO_FIT = 1;
    //IMAGE_SCALE_TO_WIDTH = 2;
    private Integer scaletofit = 0;
    private Integer width = 100;
    //ImageText
    Integer height = 100;
    private String typeface = "default";
    private Integer textsize = 20;
    private Boolean showbackground = false;
    //Barcode
    //BARCODE_TYPE_CODABAR - Indicates to print a CODEBAR Barcode
    //BARCODE_TYPE_UPCA - Indicates to print a UPCA Barcode
    //BARCODE_TYPE_UPCE - Indicates to print a UPCE Barcode
    //BARCODE_TYPE_EAN13 - Indicates to print a EAN13 Barcode
    //BARCODE_TYPE_EAN8 - Indicates to print a EAN8 Barcode
    //BARCODE_TYPE_CODE39 - Indicates to print a CODE39 Barcode
    //BARCODE_TYPE_ITF - Indicates to print a ITF Barcode
    //BARCODE_TYPE_CODE93 - Indicates to print a CODE93 Barcode
    //BARCODE_TYPE_CODE128 - Indicates to print a CODE128 Barcode
    //BARCODE_TYPE_CODE32 - Indicates to print a CODE32 Barcode
    private String barcodetype = "barcode_type_ean13";
    //BARCODE_HRI_NONE - Indicates no HRI on the Barcode
    //BARCODE_HRI_TOP - Indicates HRI on TOP of the Barcode
    //BARCODE_HRI_BOTTOM - Indicates HRI on BOTTOM of the Barcode
    //BARCODE_HRI_TOPBOTTOM - Indicates HRI on TOP and BOTTOM of the Barcode
    private String barcodehritype = "barcode_hri_none";
    //Barcode Width expressed in dot (referred to the narrowest bar) : use lower than 4
    //http://www.makebarcode.com/info/appnote/app_009.html
    private Integer barcodewidth = 4;
    //Barcode2D
    //BARCODE_TYPE_QRCODE - Indicates to print a QRCODE Barcode 2D
    //BARCODE_TYPE_PDF417 - Indicates to print a PDF417 Barcode 2D
    //BARCODE_TYPE_DATAMATRIX - Indicates to print a DATAMATRIX Barcode 2D
    //BARCODE_TYPE_AZTEC - Indicates to print a AZTEC Barcode 2D
    private String barcode2dtype = "barcode_type_qrcode";

    //Cut
    //CUT_TOTAL = 0;
    //CUT_PARTIAL = 1;
    private Boolean cut = false;
    private Integer cutmode = 0;

    public String getKey() { return key; }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Integer getFeeds() {
        return feeds;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getCharfont() {
        return charfont;
    }

    public Integer getCharheight() {
        return charheight;
    }

    public Integer getCharwidth() {
        return charwidth;
    }

    public Boolean getEmphasized() {
        return emphasized;
    }

    public Boolean getItalic() {
        return italic;
    }

    public Boolean getUnderline() {
        return underline;
    }

    public Integer getCharset() {
        return charset;
    }

    public Integer getAlign() {
        return align;
    }

    public String getSource() {
        return source;
    }

    public Integer getScaletofit() {
        return scaletofit;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getTypeface() {
        return typeface;
    }

    public Integer getTextSize() {
        return textsize;
    }

    public Boolean getShowbackground() {
        return showbackground;
    }

    public Boolean getCut() {
        return cut;
    }

    public Integer getCutmode() {
        return cutmode;
    }

    public String getBarcodetype() {
        return barcodetype;
    }

    public String getBarcodehritype() {
        return barcodehritype;
    }

    public Integer getBarCodeWidth() {
        return barcodewidth;
    }

    public String getBarcode2dtype() {
        return barcode2dtype;
    }
}
