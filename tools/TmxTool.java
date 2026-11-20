import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/**
 * sheet <png> <out.png> <col0> <col1> <row0> <row1> <scale>   -> labelled tile contact sheet
 * render <tmx> <png-sheet> <out.png> [objects]                 -> render a map (optionally with object outlines)
 * Tileset: 16px tiles, spacing 1, 57 columns (Kenney roguelike sheet).
 */
public class TmxTool {
    static final int T = 16, SP = 1;
    static int COLS = 57; // wird beim Rendern aus der .tmx gelesen (City-Tileset hat 37 Spalten)

    static BufferedImage tile(BufferedImage sheet, int gid) {
        int id = gid - 1;
        int cx = (id % COLS) * (T + SP), cy = (id / COLS) * (T + SP);
        if (cy + T > sheet.getHeight()) return null;
        return sheet.getSubimage(cx, cy, T, T);
    }

    public static void main(String[] a) throws Exception {
        if (a[0].equals("sheet")) {
            BufferedImage sheet = ImageIO.read(new File(a[1]));
            int c0 = Integer.parseInt(a[3]), c1 = Integer.parseInt(a[4]);
            int r0 = Integer.parseInt(a[5]), r1 = Integer.parseInt(a[6]);
            int sc = Integer.parseInt(a[7]);
            if (a.length > 8) COLS = Integer.parseInt(a[8]); // Spalten des Tilesets (Standard 57)
            int cell = T * sc + 4, lab = 12;
            BufferedImage out = new BufferedImage((c1 - c0 + 1) * cell, (r1 - r0 + 1) * (cell + lab), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setColor(new Color(255, 0, 255)); g.fillRect(0, 0, out.getWidth(), out.getHeight());
            g.setFont(new Font("Arial", Font.BOLD, 10));
            for (int r = r0; r <= r1; r++) for (int c = c0; c <= c1; c++) {
                int gid = r * COLS + c + 1;
                BufferedImage t = tile(sheet, gid);
                int x = (c - c0) * cell, y = (r - r0) * (cell + lab);
                if (t != null) g.drawImage(t, x + 2, y + lab, T * sc, T * sc, null);
                g.setColor(Color.BLACK); g.fillRect(x, y, cell, lab);
                g.setColor(Color.WHITE); g.drawString(String.valueOf(gid), x + 2, y + 10);
            }
            ImageIO.write(out, "png", new File(a[2]));
            return;
        }
        if (a[0].equals("render")) {
            String xml = Files.readString(new File(a[1]).toPath());
            BufferedImage sheet = ImageIO.read(new File(a[2]));
            Matcher mm = Pattern.compile("<map [^>]*width=\"(\\d+)\" height=\"(\\d+)\"").matcher(xml); mm.find();
            Matcher cm = Pattern.compile("columns=\"(\\d+)\"").matcher(xml);
            if (cm.find()) COLS = Integer.parseInt(cm.group(1));
            int W = Integer.parseInt(mm.group(1)), H = Integer.parseInt(mm.group(2));
            BufferedImage out = new BufferedImage(W * T, H * T, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            Matcher lm = Pattern.compile("<data encoding=\"csv\">(.*?)</data>", Pattern.DOTALL).matcher(xml);
            while (lm.find()) {
                String[] v = lm.group(1).replace("\n", "").replace("\r", "").split(",");
                for (int i = 0; i < v.length; i++) {
                    if (v[i].isBlank()) continue;
                    long raw = Long.parseLong(v[i].trim());
                    int gid = (int) (raw & 0x0FFFFFFFL);
                    if (gid == 0) continue;
                    BufferedImage t = tile(sheet, gid);
                    if (t != null) g.drawImage(t, (i % W) * T, (i / W) * T, null);
                }
            }
            if (a.length > 4 && a[4].equals("objects")) {
                g.setStroke(new BasicStroke(1.5f));
                Matcher om = Pattern.compile("<object [^>]*type=\"([^\"]+)\" x=\"([-\\d.]+)\" y=\"([-\\d.]+)\"(?: width=\"([\\d.]+)\" height=\"([\\d.]+)\")?\\s*(/>|>\\s*<(polygon|polyline) points=\"([^\"]+)\")").matcher(xml);
                while (om.find()) {
                    String type = om.group(1);
                    double x = Double.parseDouble(om.group(2)), y = Double.parseDouble(om.group(3));
                    g.setColor(type.startsWith("Pfad") || type.equals("PFadAussen") ? Color.MAGENTA : Color.RED);
                    if (om.group(8) != null) {
                        Path2D p = new Path2D.Double();
                        boolean first = true;
                        for (String pt : om.group(8).split(" ")) {
                            String[] xy = pt.split(",");
                            double px = x + Double.parseDouble(xy[0]), py = y + Double.parseDouble(xy[1]);
                            if (first) { p.moveTo(px, py); first = false; } else p.lineTo(px, py);
                        }
                        if ("polygon".equals(om.group(7))) p.closePath();
                        g.draw(p);
                    } else if (om.group(4) != null) {
                        g.drawRect((int) x, (int) y, (int) Double.parseDouble(om.group(4)), (int) Double.parseDouble(om.group(5)));
                    }
                }
            }
            // Optional: Laufroute einzeichnen: route=x,y;x,y;...  (Entity-Positionen)
            //           offset=dx,dy verschiebt auf die Mitte der Hitbox (Standard 14,14)
            double ox = 14, oy = 14;
            for (String o : a) if (o.startsWith("offset=")) {
                String[] v = o.substring(7).split(",");
                ox = Double.parseDouble(v[0]); oy = Double.parseDouble(v[1]);
            }
            for (String arg : a) {
                if (!arg.startsWith("route=")) continue;
                String[] pts = arg.substring(6).split(";");
                Path2D p = new Path2D.Double();
                for (int k = 0; k < pts.length; k++) {
                    String[] xy = pts[k].split(",");
                    double px = Double.parseDouble(xy[0]) + ox, py = Double.parseDouble(xy[1]) + oy;
                    if (k == 0) p.moveTo(px, py); else p.lineTo(px, py);
                }
                g.setStroke(new BasicStroke(3f));
                g.setColor(new Color(0, 90, 255));
                g.draw(p);
                g.setFont(new Font("Arial", Font.BOLD, 13));
                for (int k = 0; k < pts.length; k++) {
                    String[] xy = pts[k].split(",");
                    int px = (int) (Double.parseDouble(xy[0]) + ox), py = (int) (Double.parseDouble(xy[1]) + oy);
                    g.setColor(Color.WHITE); g.fillOval(px - 4, py - 4, 8, 8);
                    g.setColor(Color.BLACK); g.drawString(String.valueOf(k), px + 5, py - 5);
                }
            }
            // Optional: Ausschnitt vergrößern: crop=x,y,w,h,skala
            for (String arg : a) {
                if (!arg.startsWith("crop=")) continue;
                String[] v = arg.substring(5).split(",");
                int cx = Integer.parseInt(v[0]), cy = Integer.parseInt(v[1]);
                int cw = Integer.parseInt(v[2]), ch = Integer.parseInt(v[3]), sk = Integer.parseInt(v[4]);
                BufferedImage gross = new BufferedImage(cw * sk, ch * sk, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gg = gross.createGraphics();
                gg.drawImage(out.getSubimage(cx, cy, cw, ch), 0, 0, cw * sk, ch * sk, null);
                gg.setColor(Color.YELLOW);
                gg.setFont(new Font("Arial", Font.PLAIN, 10));
                for (int x = (cx / 16 + 1) * 16; x < cx + cw; x += 16) {      // Raster alle 16px mit Koordinaten
                    gg.drawLine((x - cx) * sk, 0, (x - cx) * sk, 4);
                    if (x % 32 == 0) gg.drawString(String.valueOf(x), (x - cx) * sk + 2, 12);
                }
                for (int y = (cy / 16 + 1) * 16; y < cy + ch; y += 16) {
                    gg.drawLine(0, (y - cy) * sk, 4, (y - cy) * sk);
                    if (y % 32 == 0) gg.drawString(String.valueOf(y), 6, (y - cy) * sk + 4);
                }
                out = gross;
            }
            ImageIO.write(out, "png", new File(a[3]));
        }
    }
}
