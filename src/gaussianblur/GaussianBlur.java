package gaussianblur;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

class GaussianBlur {
    public static double[][] kernelO;

    public static double generateKernel(double sigma) {
        //create kernel
        int kernelSize = (int) (6 * sigma + 1);
        kernelSize = kernelSize % 2 == 0 ? kernelSize + 1 : kernelSize;
        double[][] kernel = new double[kernelSize][kernelSize];
        double sum = 0.0;
        int x0 = kernelSize / 2;
        int y0 = kernelSize / 2;
        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                double value = Math.exp(-0.5 * (Math.pow((x - x0) / sigma, 2.0) + Math.pow((y - y0) / sigma, 2.0)));
                kernel[x][y] = value;
                sum += value;
            }
        }
        
        double maxValue = 0;
        //normalize kernel
        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                kernel[x][y] /= sum;
                maxValue = maxValue > kernel[x][y] ? maxValue : kernel[x][y];
            }
        }

        sendKernel(kernel);
        return maxValue;
    }
    
    public static void sendKernel(double[][] kernel){
        kernelO = kernel;
    }
    
    public static double[][] getKernel(){
        return kernelO;
    }
    
    public static BufferedImage convolution(BufferedImage image, double[][] kernel, int sigma){
        int width = image.getWidth();
        int height = image.getHeight();
        int kernelSize = (6 * sigma + 1);
        kernelSize = kernelSize % 2 == 0 ? kernelSize + 1 : kernelSize;
        //double[][] kernel = new double[kernelSize][kernelSize];
        double sum = 0.0;
        int x0 = kernelSize / 2;
        int y0 = kernelSize / 2;
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        
        int[] color = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                color[i * width + j] = image.getRGB(j, i);
            }
        }

        int[] newColor = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double r = 0, g = 0, b = 0;
                for (int ky = -y0; ky <= y0; ky++) {
                    for (int kx = -x0; kx <= x0; kx++) {
                        if (x + kx >= 0 && x + kx < width && y + ky >= 0 && y + ky < height) {
                            int c = color[(y + ky) * width + (x + kx)];
                            double weight = kernel[kx + x0][ky + y0];
                            r += weight * ((c >> 16) & 0xff);
                            g += weight * ((c >> 8) & 0xff);
                            b += weight * (c & 0xff);
                        }
                    }
                }
                newColor[y * width + x] = (255 << 24) | ((int) r << 16) | ((int) g << 8) | (int) b;
            }
        }
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output.setRGB(x, y, newColor[y * width + x]);
            }
        }
        return output;
    }
    
    public static void showPicture(BufferedImage img, String title) {
        JFrame jf = new JFrame();
        Dimension dm = new Dimension(500, 500);
        jf.setSize(dm);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setTitle(title);

        jf.add(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(img.getScaledInstance((int) dm.getWidth(), (int) dm.getHeight(), Image.SCALE_SMOOTH), 0, 0, this);
            }
        });
        jf.revalidate();
    }
    
    public static void printDistribution(float[][] org, float max) {
        int size = org.length;
        JFrame f = new JFrame();
        Dimension dim = new Dimension(250, 250);
        f.setSize(dim);
        f.setLocationRelativeTo(null);
        f.setResizable(true);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel jp = new JPanel();
        GridLayout gl = new GridLayout(size, size);
        jp.setLayout(gl);

        f.add(jp);
        float[][] values = org;/*{
            {0.003f, 0.013f, 0.022f, 0.013f, 0.003f},
            {0.013f, 0.060f, 0.098f, 0.060f, 0.013f},
            {0.022f, 0.098f, 0.162f, 0.098f, 0.022f},
            {0.013f, 0.060f, 0.098f, 0.060f, 0.013f},
            {0.003f, 0.013f, 0.022f, 0.013f, 0.003f}
            };*/
        Color colVal[] = {Color.blue, Color.cyan, Color.green, Color.yellow, Color.red};

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                /*Random r = new Random();
                Color c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));*/
                JPanel cp = new JPanel();
                cp.setBorder(BorderFactory.createLineBorder(Color.black));
                //System.out.println(max);
                cp.setBackground(colVal[(int) (values[i][j] * (colVal.length - 1) / max)]);
                jp.add(cp);
            }
            jp.revalidate();
        }
    }
    
    public static float[][] getMatrixfromVector(float[] vector) {
        int kernelSize = (int) Math.sqrt(vector.length);
        float[][] matrix = new float[kernelSize][kernelSize];
        for (int i = 0, iA = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = vector[iA];
                iA++;
            }
        }
        return matrix;
    }

    public static float[] getVectorFromMatrix(float[][] matrix) {
        float[] vect = new float[matrix.length * matrix[0].length];
        for (int i = 0, iA = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                vect[iA] = matrix[i][j];
            }
        }
        return vect;
    }

    public static float maxValue(float[][] matrix) {
        float i = 0f;
        for (float[] x : matrix) {
            for (float y : x) {
                if (y > i) {
                    i = y;
                }
            }
        }
        return i;
    }

    public static float maxValue(double[][] matrix) {
        float i = 0f;
        for (double[] x : matrix) {
            for (double y : x) {
                if (y > i) {
                    i = (float) y;
                }
            }
        }
        return i;
    }

    public static float[][] doubleToFloatMatrix(double[][] matrix) {
        float[][] mat = new float[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                mat[i][j] = (float) matrix[i][j];
            }
        }
        return mat;
    }

    public static double[][] floatToDoubleMatrix(float[][] matrix) {
        double[][] mat = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                mat[i][j] = matrix[i][j];
            }
        }
        return mat;
    }
    
    public static void main(String[] args) throws Exception{
        String filename = "a.jpg";
        File root = new File("C:\\Users\\modim\\Desktop\\" + filename);
        BufferedImage imgOrg = ImageIO.read(root);
        int sigma = 5;
        
        double max = generateKernel(sigma);
        double[][] kernel = getKernel();
        BufferedImage res = convolution(imgOrg, kernel, sigma);
        
        printDistribution(doubleToFloatMatrix(kernel), (float) max);
        showPicture(res, "convolved blur result");
    }
    
        public static BufferedImage takeScreenshot(JFrame f) {
        JFrame frame = f;
        f.setVisible(false);
        BufferedImage bi = null;
        // Set up the JFrame as desired...

        try {
            Robot robot = new Robot();
            Rectangle rect = frame.getContentPane().getBounds();
            rect.x += frame.getX();
            rect.y += frame.getY();
            bi = robot.createScreenCapture(rect);
            
            // Save the image to a file or do something else with it...
        } catch (AWTException e) {
            e.printStackTrace();
        }
        f.setVisible(true);
        
        return bi;
    }
}
