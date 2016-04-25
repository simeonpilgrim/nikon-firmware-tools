namespace WindowsFormsApplication1
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.model_lb = new System.Windows.Forms.Label();
            this.firmware_lb = new System.Windows.Forms.Label();
            this.btBackupEeprom = new System.Windows.Forms.Button();
            this.model_tb = new System.Windows.Forms.TextBox();
            this.firmware_tb = new System.Windows.Forms.TextBox();
            this.btRestoreEeprom = new System.Windows.Forms.Button();
            this.lang_lb = new System.Windows.Forms.Label();
            this.langage_tb = new System.Windows.Forms.TextBox();
            this.btUnlockLanguage = new System.Windows.Forms.Button();
            this.btEmailDeveloper = new System.Windows.Forms.Button();
            this.btSearchForCamera = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // model_lb
            // 
            this.model_lb.AutoSize = true;
            this.model_lb.Location = new System.Drawing.Point(12, 15);
            this.model_lb.Name = "model_lb";
            this.model_lb.Size = new System.Drawing.Size(46, 13);
            this.model_lb.TabIndex = 0;
            this.model_lb.Text = "Camera:";
            // 
            // firmware_lb
            // 
            this.firmware_lb.AutoSize = true;
            this.firmware_lb.Location = new System.Drawing.Point(12, 41);
            this.firmware_lb.Name = "firmware_lb";
            this.firmware_lb.Size = new System.Drawing.Size(52, 13);
            this.firmware_lb.TabIndex = 2;
            this.firmware_lb.Text = "Firmware:";
            // 
            // btBackupEeprom
            // 
            this.btBackupEeprom.Enabled = false;
            this.btBackupEeprom.Location = new System.Drawing.Point(12, 64);
            this.btBackupEeprom.Name = "btBackupEeprom";
            this.btBackupEeprom.Size = new System.Drawing.Size(129, 24);
            this.btBackupEeprom.TabIndex = 4;
            this.btBackupEeprom.Text = "Backup Eeprom";
            this.btBackupEeprom.UseVisualStyleBackColor = true;
            // 
            // model_tb
            // 
            this.model_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.model_tb.Location = new System.Drawing.Point(73, 12);
            this.model_tb.Name = "model_tb";
            this.model_tb.ReadOnly = true;
            this.model_tb.Size = new System.Drawing.Size(345, 20);
            this.model_tb.TabIndex = 5;
            this.model_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // firmware_tb
            // 
            this.firmware_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.firmware_tb.Location = new System.Drawing.Point(73, 38);
            this.firmware_tb.Name = "firmware_tb";
            this.firmware_tb.ReadOnly = true;
            this.firmware_tb.Size = new System.Drawing.Size(345, 20);
            this.firmware_tb.TabIndex = 6;
            this.firmware_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // btRestoreEeprom
            // 
            this.btRestoreEeprom.Enabled = false;
            this.btRestoreEeprom.Location = new System.Drawing.Point(147, 64);
            this.btRestoreEeprom.Name = "btRestoreEeprom";
            this.btRestoreEeprom.Size = new System.Drawing.Size(129, 24);
            this.btRestoreEeprom.TabIndex = 7;
            this.btRestoreEeprom.Text = "Restore Eeprom";
            this.btRestoreEeprom.UseVisualStyleBackColor = true;
            // 
            // lang_lb
            // 
            this.lang_lb.AutoSize = true;
            this.lang_lb.Location = new System.Drawing.Point(12, 114);
            this.lang_lb.Name = "lang_lb";
            this.lang_lb.Size = new System.Drawing.Size(55, 13);
            this.lang_lb.TabIndex = 8;
            this.lang_lb.Text = "Language";
            // 
            // langage_tb
            // 
            this.langage_tb.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.langage_tb.Location = new System.Drawing.Point(73, 111);
            this.langage_tb.Name = "langage_tb";
            this.langage_tb.ReadOnly = true;
            this.langage_tb.Size = new System.Drawing.Size(345, 20);
            this.langage_tb.TabIndex = 9;
            this.langage_tb.Text = "Camera Not Detected, plug in via USB";
            // 
            // btUnlockLanguage
            // 
            this.btUnlockLanguage.Enabled = false;
            this.btUnlockLanguage.Location = new System.Drawing.Point(12, 137);
            this.btUnlockLanguage.Name = "btUnlockLanguage";
            this.btUnlockLanguage.Size = new System.Drawing.Size(129, 24);
            this.btUnlockLanguage.TabIndex = 10;
            this.btUnlockLanguage.Text = "Unlock Language";
            this.btUnlockLanguage.UseVisualStyleBackColor = true;
            // 
            // btEmailDeveloper
            // 
            this.btEmailDeveloper.Location = new System.Drawing.Point(12, 167);
            this.btEmailDeveloper.Name = "btEmailDeveloper";
            this.btEmailDeveloper.Size = new System.Drawing.Size(129, 23);
            this.btEmailDeveloper.TabIndex = 11;
            this.btEmailDeveloper.Text = "Email Developer";
            this.btEmailDeveloper.UseVisualStyleBackColor = true;
            // 
            // btSearchForCamera
            // 
            this.btSearchForCamera.Location = new System.Drawing.Point(283, 65);
            this.btSearchForCamera.Name = "btSearchForCamera";
            this.btSearchForCamera.Size = new System.Drawing.Size(135, 23);
            this.btSearchForCamera.TabIndex = 13;
            this.btSearchForCamera.Text = "Search For Camera";
            this.btSearchForCamera.UseVisualStyleBackColor = true;
            this.btSearchForCamera.Click += new System.EventHandler(this.button4_Click);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(430, 289);
            this.Controls.Add(this.btSearchForCamera);
            this.Controls.Add(this.btEmailDeveloper);
            this.Controls.Add(this.btUnlockLanguage);
            this.Controls.Add(this.langage_tb);
            this.Controls.Add(this.lang_lb);
            this.Controls.Add(this.btRestoreEeprom);
            this.Controls.Add(this.firmware_tb);
            this.Controls.Add(this.model_tb);
            this.Controls.Add(this.btBackupEeprom);
            this.Controls.Add(this.firmware_lb);
            this.Controls.Add(this.model_lb);
            this.MinimumSize = new System.Drawing.Size(305, 0);
            this.Name = "MainForm";
            this.Text = "LanguageFix";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label model_lb;
        private System.Windows.Forms.Label firmware_lb;
        private System.Windows.Forms.Button btBackupEeprom;
        private System.Windows.Forms.TextBox model_tb;
        private System.Windows.Forms.TextBox firmware_tb;
        private System.Windows.Forms.Button btRestoreEeprom;
        private System.Windows.Forms.Label lang_lb;
        private System.Windows.Forms.TextBox langage_tb;
        private System.Windows.Forms.Button btUnlockLanguage;
        private System.Windows.Forms.Button btEmailDeveloper;
        private System.Windows.Forms.Button btSearchForCamera;
    }
}

