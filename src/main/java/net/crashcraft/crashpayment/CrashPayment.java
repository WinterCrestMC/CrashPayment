package net.crashcraft.crashpayment;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.crashcraft.crashpayment.payment.PaymentProvider;
import net.crashcraft.crashpayment.payment.ProcessorManager;
import net.crashcraft.crashpayment.payment.ProviderInitializationException;
import net.crashcraft.crashpayment.payment.commands.TokenCommands;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

public class CrashPayment extends JavaPlugin {
    private BukkitCommandManager<CommandSender> manager;
    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private AnnotationParser<CommandSender> annotationParser;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupCommands();
        registerCommands();
    }

    private void setupCommands() {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                CommandExecutionCoordinator.simpleCoordinator();
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    /* Owning plugin */ this,
                    /* Coordinator function */ executionCoordinatorFunction,
                    /* Command Sender -> C */ mapperFunction,
                    /* C -> Command Sender */ mapperFunction
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Use contains to filter suggestions instead of default startsWith
        this.manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));

        //
        // Create a BukkitAudiences instance (adventure) in order to use the minecraft-extras
        // help system
        //
        this.bukkitAudiences = BukkitAudiences.create(this);

        //
        // Create the Minecraft help menu system
        //
        this.minecraftHelp = new MinecraftHelp<>(
                /* Help Prefix */ "/crashpayments help",
                /* Audience mapper */ this.bukkitAudiences::sender,
                /* Manager */ this.manager
        );
        //
        // Register Brigadier mappings
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        //
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        //
        this.confirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColor.RED + "Confirmation required. Confirm using /crashpayments confirm."),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColor.RED + "You don't have any pending commands.")
        );
        //
        // Register the confirmation processor. This will enable confirmations for commands that require it
        //
        this.confirmationManager.registerConfirmationProcessor(this.manager);

        //
        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        //
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple()
                        // This will allow you to decorate commands with descriptions
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();
        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.manager,
                /* Command sender type */ CommandSender.class,
                /* Mapper for command meta instances */ commandMetaFunction
        );
        //
        // Override the default exception handlers
        //
        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("CrashPayments", NamedTextColor.AQUA))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(this.manager, this.bukkitAudiences::sender);
    }

    private void registerCommands() {
        this.annotationParser.parse(new TokenCommands());
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin){
        return setupPaymentProvider(plugin, "");
    }

    public ProcessorManager setupPaymentProvider(JavaPlugin plugin, String providerOverride){
        try {
            return new ProcessorManager(plugin, providerOverride);
        } catch (ProviderInitializationException e){
            e.printStackTrace();
        }
        return null;
    }

    public void register(JavaPlugin plugin, ServicePriority priority, PaymentProvider provider){
        Bukkit.getServicesManager().register(PaymentProvider.class, provider, plugin, priority);
        plugin.getLogger().info("Registered Payment Provider [" + provider.getProviderIdentifier() + "] with priority " + priority.name());
    }

    static public CrashPayment getInstance(){
        return JavaPlugin.getPlugin(CrashPayment.class);
    }

    static public ItemStack setCMD(Integer num, ItemStack items) {
        ItemMeta meta = items.getItemMeta();
        if (meta == null) {
            getInstance().getLogger()
                    .warning("Itemmeta for this material is null, please change this in the config.");
            return items;
        }
        meta.setCustomModelData(num);
        items.setItemMeta(meta);
        return items;
    }
}
