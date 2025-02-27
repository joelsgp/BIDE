package zezombye.bide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ONLY EVER USE CasioStrings WHEN HANDLING CASIO ENCODING!
 * The reason is that Strings use UTF-16, and invalid encodings are replaced by '?'
 * Try to create the string with byte[]{0xAA, 0xAC, 0xBD, 0xAF, 0x90, 0x88, 0x9A, 0x8D}.
 * You'll see that some characters are replaced by '?'.
 */


public class G1MWrapper {

    public List<CasioString> parts = new ArrayList<>();

    public void addPart(CasioString part, CasioString partName, int type) {
        if (partName.length() > 8) {
            partName = partName.substring(0, 8);
        }

        // Subheader
        byte[] padding = {0, 0, 0, 0, 0, 0, 0, 0};
        CasioString sizeString = new CasioString(new byte[]{0,0,0,0});
        for (int i = 0; i < 4; i++) {
            sizeString.setCharAt(i, (byte)(part.length()>>(8*(3-i))));
        }
        if (sizeString.length() != 4) {
            BIDE.error("Size string length isn't 4!");
        }

        // Subheader
        CasioString subheader = getSubHeaderIDAndDir(type, partName.toString());
        subheader.add(partName);
        subheader.add(Arrays.copyOfRange(padding, 0, 8-partName.length()));
        int id = getPartID(type);
        if (id < 0) return;
        subheader.add(id);
        subheader.add(sizeString);
        subheader.add(new byte[]{0,0,0});
        part.add(0, subheader);

        parts.add(part);
    }

    public void generateG1M(String destPath) {
        // Header
        CasioString content = new CasioString();
        for (CasioString part : parts) {
            content.add(part);
        }
        CasioString sizeString = new CasioString(new byte[]{0,0,0,0});

        for (int i = 0; i < 4; i++) {
            sizeString.setCharAt(i, (byte)((content.length()+0x20)>>(8*(3-i))));
        }
        CasioString header = new CasioString(new byte[]{'U','S','B','P','o','w','e','r', 0x31, 0,0x10,0,0x10,0, (byte)(sizeString.charAt(3)+0x41), 1});
        header.add(sizeString);
        header.add((sizeString.charAt(3)+0xB8)%0x100);
        header.add(new byte[]{(byte)0xBD, (byte)0xB6, (byte)0xBB, (byte)0xBA, (byte)0xDF, (byte)0x8F, (byte)0x8D, (byte)0x90, (byte)0x98, (byte)(parts.size()>>8), (byte)parts.size()});
        CasioString header2 = new CasioString();
        for (int i = 0; i < header.length(); i++) {
            header2.add(0xFF-header.charAt(i));
        }
        content.add(0, header2);
        IO.writeToFile(new File(destPath), content.getContent(), true);
        if (BIDE.runOn.equals("emulator")) {
            BIDE.autoImport.autoImport(destPath);
        }
    }

    public int getPartID(int partType) {
        switch (partType) {
            case BIDE.TYPE_PROG:
                return 0x01;
            case BIDE.TYPE_PICT:
                return 0x07;
            case BIDE.TYPE_CAPT:
                return 0x0A;
            default:
                BIDE.error("Unknown part type "+ partType);
                return -1;
        }
    }

    // Not really necessary to use CasioStrings here
    // Only special bytes are 0 and 1
    public CasioString getSubHeaderIDAndDir(int partType, String partName) {
        StringBuilder partTypeStr;
        StringBuilder partDir;
        switch (partType) {
            case BIDE.TYPE_PROG -> {
                partTypeStr = new StringBuilder("PROGRAM");
                partDir = new StringBuilder("system");
            }
            case BIDE.TYPE_PICT -> {
                partTypeStr = new StringBuilder("PICTURE " + partName.substring(4));
                partDir = new StringBuilder("main");
            }
            case BIDE.TYPE_CAPT -> {
                partTypeStr = new StringBuilder("CAPT " + partName.substring(4));
                partDir = new StringBuilder("@REV2");
            }
            default -> {
                BIDE.error("Unknown part ID " + partType);
                return null;
            }
        }

        // padding
        int lenId = partTypeStr.length();
        partTypeStr.append("0".repeat(Math.max(0, 16 - lenId)));
        int lenDir = partDir.length();
        partDir.append("0".repeat(Math.max(0, 8 - lenDir)));

        return new CasioString(partTypeStr + new String(new char[]{'0', '0', '0', '1'}) + partDir);
    }
}
