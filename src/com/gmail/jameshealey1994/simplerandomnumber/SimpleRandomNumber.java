package com.gmail.jameshealey1994.simplerandomnumber;

/**
 *
 * @author James Healey
 */
import java.util.Random;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
 
public final class SimpleRandomNumber extends JavaPlugin implements Listener
{
    private int FALLBACK_MIN = 1;
    private int FALLBACK_MAX = 6;
    private int defaultMin = FALLBACK_MIN;
    private int defaultMax = FALLBACK_MAX;

    @Override
    public void onEnable()
    {
        // Save a copy of the default config.yml if one is not there
        this.saveDefaultConfig();
        
        setDefaults();
    }
    
    /**
     * Sets default Min and Max values from the configuration file.
     * If values from the file are invalid (min > max), defaults of 1 and 6 are used.
     */
    private void setDefaults()
    {
        // Check config values for defaults are ok (max >= min)
        if (areDefaultsOK())
        {
            // If config values are OK, use as defaults
            setDefaultMin(getConfig().getInt("DefaultMinimum"));
            setDefaultMax(getConfig().getInt("DefaultMaximum"));
        }
        else
        {
            // If not, log to server and use 1 and 6 as defaults
            getLogger().log(Level.WARNING, "Default values in config are invalid, using {0} and {1} as defaults.", new Object[]{FALLBACK_MIN, FALLBACK_MAX});
            
        }
    }
    
    /**
     * Returns whether the default minimum and maximum values from the file are valid (max > min)
     * @return if (max from file >= min from file)
     */
    private boolean areDefaultsOK()
    {
        return (getConfig().getInt("DefaultMaximum") >= getConfig().getInt("DefaultMinimum"));
    }
    
    /**
     * Carries out commands
     * @param sender The user who sent the command
     * @param cmd The command the user sent
     * @param commandLabel The exact command the user sent
     * @param args The arguments given with the command
     * @return If a command was used correctly
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("srn"))
        {
            if (args.length >= 1)
            {
                if (areInts(args))
                {
                    if (sender.hasPermission("srn.custom"))
                    {
                        return rollDice(sender, args);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You need permission 'srn.custom' to use this command.");
                        return false;
                    }
                }
                else if (args[0].equalsIgnoreCase("setmin")) // Set default minimum
                {
                    if (sender.hasPermission("srn.admin"))
                    {
                        return setMin(sender, args);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
                        return false;
                    }
                }
                else if (args[0].equalsIgnoreCase("setmax")) // Set default maximum
                {
                    if (sender.hasPermission("srn.admin"))
                    {
                        return setMax(sender, args);
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
                        return false;
                    }
                }
                // Useful if user changes config values from file.
                else if (args[0].equalsIgnoreCase("reload")) // Reload values from config.
                {
                    if (sender.hasPermission("srn.admin"))
                    {
                        reload(sender);
                        return true;
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
                        return false;
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.RED + "Custom range values need to be integers!");
                    return false;
                }
            }
            else
            {
                if (sender.hasPermission("srn.defaults"))
                {
                    broadcastResult(sender, defaultMin, defaultMax); // /roll - Roll dice with default values
                    return true;
                }
                else
                {
                    sender.sendMessage(ChatColor.RED + "You need permission 'srn.defaults' to use this command.");
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Displays a random number between 2 limits, either default or specified.
     * @param sender The user who sent the command
     * @param args The arguments given with the command, possibly including min and max values
     * @return If a command was used correctly
     */
    private boolean rollDice(CommandSender sender, String[] args)
    {
        if (args.length == 1) // roll <max>
        {
            int max = Integer.valueOf(args[0]);
            if (defaultMin <= max)
            {
                broadcastResult(sender, defaultMin, max);
                return true;
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "Max (" + max + ") lower than the default min (" + defaultMin + ")!");
                return false;
            }
        }
        else if (args.length == 2) // roll <min> <max>
        {
            int min = Integer.valueOf(args[0]);
            int max = Integer.valueOf(args[1]);
            if (min <= max)
            {
                broadcastResult(sender, min, max);
                return true;
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "Max (" + max + ") lower than your min (" + min + ")!");
                return false;
            }
        }
        else if (args.length != 0) // not defaults and not custom
        {
            sender.sendMessage(ChatColor.RED + "Invalid number of arguments!");
            return false;
        }
        return false;
    }
    
    /**
     * Checks if the strings passed can be converted to integers.
     * @param strings
     * @return If the array of strings passed can be converted to integers.
     */
    private boolean areInts(String[] strings)
    {
        for (int i = 0; i < strings.length; i++)
        {
            try
            {
                Integer.parseInt(strings[i]);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the default minimum value.
     * @param sender The user who sent the command
     * @param args The arguments given with the command, including the new min value
     * @return If a new default is set correctly.
     */
    private boolean setMin(CommandSender sender, String[] args)
    {
        if (args.length == 2)
        {
            try
            {
                Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + "Default Minimum needs to be an integer!");
                return false;
            }
            
            if ((Integer.valueOf(args[1])) <= defaultMax)
            {
                defaultMin = (Integer.valueOf(args[1]));
                getConfig().set("DefaultMinimum", defaultMin);
                saveConfig();
                sender.sendMessage("Default Minimum set to " + defaultMin);
                return true;
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "Attempted new default min (" + Integer.valueOf(args[1]) + ") higher than current default max (" + defaultMax + ")!");
                return false;
            }
        }
        else if (args.length > 2)
        {
            sender.sendMessage(ChatColor.RED + "Too many arguments!");
            return false;
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "You need to specify a new minimum!");
            return false;
        }
    }
    
    /**
     * Sets the default maximum value.
     * @param sender The user who sent the command
     * @param args The arguments given with the command, including the new max value
     * @return If a new default is set correctly.
     */
    private boolean setMax(CommandSender sender, String[] args)
    {
        if (args.length == 2)
        {
            try
            {
                Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + "Default Maximum needs to be an integer!");
                return false;
            }
            
            if ((Integer.valueOf(args[1])) >= defaultMin)
            {
                defaultMax = (Integer.valueOf(args[1]));
                getConfig().set("DefaultMaximum", defaultMax);
                saveConfig();
                sender.sendMessage("Default Maximum set to " + defaultMax);
                return true;
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "Attempted new default max (" + Integer.valueOf(args[1]) + ") lower than current default min (" + defaultMin + ")!");
                return false;
            }
        }
        else if (args.length > 2)
        {
            sender.sendMessage(ChatColor.RED + "Too many arguments!");
            return false;
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "You need to specify a new maximum!");
            return false;
        }
    }

    /**
     * Reloads values from configuration file
     * @param sender The user who sent the command
     */
    private void reload(CommandSender sender)
    {
        reloadConfig();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Configuration reloaded.");
        setDefaults();
        sender.sendMessage("Current default min:" + defaultMin);
        sender.sendMessage("Current default max:" + defaultMax);
    }
    
    /**
     * Broadcasts a random number between 2 limits
     * @param sender The user who sent the command
     * @param min The minimum value the random number can be
     * @param max The maximum value the random number can be
     */
    private void broadcastResult(CommandSender sender, int min, int max)
    {        
        int result = getRandom(min, max);

        String message = getConfig().getString("BroadcastMessage");
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = message.replaceAll("-sender", sender.getName());
        message = message.replaceAll("-min", String.valueOf(min));
        message = message.replaceAll("-max", String.valueOf(max));
        message = message.replaceAll("-result", String.valueOf(result));
        
        getServer().broadcastMessage(message);
    }
    
    /**
     * Generates a random number between 2 limits
     * @param min The minimum value the random number can be
     * @param max The maximum value the random number can be
     * @return A random number between min and max
     */
    private int getRandom(int min, int max)
    {
        Random rand = new Random();        
        // nextInt is normally exclusive of the top value, so add 1 to make it inclusive

        return rand.nextInt(max - min + 1) + min;
    }

    /*
     * Getter for variable: defaultMin
     */
    public int getDefaultMin()
    {
        return defaultMin;
    }

    /**
     * Setter for variable: defaultMin
     * @param newMin
     * @return If new value was set
     */
    public boolean setDefaultMin(int newMin)
    {
        if (newMin > getDefaultMax())
        {
            return false;
        }
        this.defaultMin = newMin;
        return true;
    }

    /*
     * Getter for variable: defaultMax
     */
    public int getDefaultMax()
    {
        return defaultMax;
    }

    /**
     * Setter for variable: defaultMax
     * @param newMax
     * @return If new value was set
     */
    public boolean setDefaultMax(int newMax)
    {
        if (newMax < getDefaultMin())
        {
            return false;
        }
        this.defaultMax = newMax;
        return true;
    }
}