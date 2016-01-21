package com.bitsyko.liblayers.layerfiles;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.helper.AndroidXMLDecompress;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public abstract class LayerFile implements Comparable<LayerFile> {

    private static final String PACKAGE_REGEX = "targetPackage=\"(.*?)\"";
    protected Layer parentLayer;
    protected String name;
    protected File file;
    protected String packageName;

    public LayerFile(Layer parentLayer, String name) {
        this.parentLayer = parentLayer;
        this.name = name;
    }

    public abstract File getFile(Context context);

    public String getPackageName(Context context) {
        if (TextUtils.isEmpty(packageName)) {
            if (file == null) {
                file = getFile(context);
            }
            PackageInfo info =
                    context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0);
            if (info != null) {
                packageName = info.packageName;
            } else {
                packageName = getTargetPackage() + ".overlay";
            }
        }
        return packageName;
    }

    public String getNiceName() {

        String name = this.name;

        //Remove .apk and .zip
        name = StringUtils.replaceEach(name, new String[]{".apk", ".zip"}, new String[]{"", ""});

        //Remove plugin name
        name = StringUtils.removeStartIgnoreCase(name,
                StringUtils.deleteWhitespace(parentLayer.getName()));

        //Remove plugin name pt.2
        name = StringUtils.removeStartIgnoreCase(name,
                StringUtils.replace(parentLayer.getName(), " ", "_"));

        //Replace _ with " "
        name = StringUtils.replace(name, "_", " ");

        //remove whitespaces from start and end
        name = StringUtils.strip(name);

        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isColor() {
        return this instanceof ColorOverlay;
    }

    public boolean isCustom() {
        return this instanceof CustomStyleOverlay;
    }

    public Layer getLayer() {
        return parentLayer;
    }

    public String getTargetPackage() {

        if (file == null) {
            throw new RuntimeException("No file to work on");
        }

        ZipFile zip;
        InputStream manifestInputStream;
        byte[] array;

        try {
            zip = new ZipFile(file);
            manifestInputStream = zip.getInputStream(zip.getEntry("AndroidManifest.xml"));
            array = IOUtils.toByteArray(manifestInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        Pattern pattern = Pattern.compile(PACKAGE_REGEX, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(AndroidXMLDecompress.decompressXML(array));

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "Can't find related package";
        }
    }

    @Override
    public String toString() {
        return getNiceName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LayerFile layerFile = (LayerFile) o;

        return new EqualsBuilder()
                .append(parentLayer, layerFile.parentLayer)
                .append(name, layerFile.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(parentLayer)
                .append(name)
                .toHashCode();
    }

    @Override
    public int compareTo(@NonNull LayerFile another) {
        return getNiceName().compareTo(another.getNiceName());
    }


}
