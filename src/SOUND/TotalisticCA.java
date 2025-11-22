package SOUND;
import java.util.Random;

// Java class for implementing 1D cellular automata
public class TotalisticCA {

  private int state_nr = 2;
  private int radius = 1;
  private long rule_nr = 2;
  private int[] ruletable = ca_rule(state_nr, radius, rule_nr);

  // Initialization
  public TotalisticCA(int k, int r, long nr) {
    if (state_nr != k || radius != r || rule_nr != nr) {
      if (k > 1) state_nr = k;
      if (r >= 0) radius = r;
      if (nr >= 0) rule_nr = nr;
      ruletable = ca_rule(state_nr, radius, rule_nr);
    }
  }

  // Construction of the rule table
  private int[] ca_rule(int k, int r, long nr) {
    int n = (k-1)*(2*r+1) + 1;
    int[] table = new int[n];
    for (int i = 1; i <= n; i++) 
      table[i-1] = rule_value(i, nr, k);
    return table;
  }

  // Help routine for making rule tables
  private int rule_value(int b, long n, int k) {
    if (b == 1)
      return (int)(n % k);
    else
      return rule_value(b-1, n/k, k);
  }

  // Generation of an initial configuration
  public int[] config_init(int n) {
    Random random_number = new Random();
    int[] array = new int[n];
    for (int i = 0; i < n; i++) {
      array[i] = (int)(state_nr*random_number.nextDouble());
      if (array[i] > state_nr-1)
	array[i] = state_nr-1;
    }
    return array;
  }

  // Computing the next configuration
  public int[] ca_next(int[] config) {
    int len = config.length;
    int[] res = new int[len];
    for (int i = 0; i < len; i++) {
      res[i] = 0;
      for (int j = -radius; j <= radius; j++) {
	if (i+j >= 0 && i+j < len)
	  res[i] += config[i+j];
	else if (i+j < 0)
	  res[i] += config[len-i+j];
	else
	  res[i] += config[i+j-len];
      }
      res[i] = ruletable[res[i]];
    }
    return res;
  }
      
}
